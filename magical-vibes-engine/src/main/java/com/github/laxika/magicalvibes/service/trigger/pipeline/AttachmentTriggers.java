package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.AsThisBecomesAttachedEffect;
import com.github.laxika.magicalvibes.model.effect.AttachedCreatureBecomesCopyOfExiledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.rules.AttachmentEvent;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.normalfx.AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ChooseCardNameAtResolutionEffectHandler;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Triggers the attachment abilities of an {@link AttachmentEvent} (CR 603.2), each handed whole to the stack door with
 * the record of the event. The abilities are read from {@link TriggerIndex} as the layers leave them; an ability's
 * event condition is judged here, its intervening "if" (CR 603.4) and limit (CR 603.2h: Inchblade Companion's "this
 * ability triggers only once each turn") by the door.
 *
 * <ul>
 *   <li>An Aura attached: the enchanted permanent's "whenever an Aura becomes attached to this", then its controller's
 *       "whenever an Aura you control becomes attached to a creature you control" (Siona) and "… to a nonland
 *       permanent an opponent controls with mana value less than or equal to that Aura's mana value" (Eriette).</li>
 *   <li>An Equipment attached to a creature: its own "whenever this Equipment becomes attached to a creature".</li>
 *   <li>An Equipment unattached: its own "whenever this Equipment becomes unattached from a permanent" (CR 701.3d).</li>
 * </ul>
 */
@Slf4j
@Service
public class AttachmentTriggers {

    private static final Trigger AURA_TO_THIS = Triggers.auraBecomesAttachedToThis();
    private static final Trigger AURA_TO_YOUR_CREATURE = Triggers.auraYouControlBecomesAttachedToCreatureYouControl();
    private static final Trigger AURA_TO_OPPONENT_NONLAND_PERMANENT =
            Triggers.auraYouControlBecomesAttachedToNonlandPermanentOpponentControls();
    private static final Trigger EQUIPMENT_ATTACHED = Triggers.becomesAttachedToCreature();
    private static final Trigger EQUIPMENT_UNATTACHED = Triggers.becomesUnattachedFromPermanent();

    private final EventAbilitySupport abilities;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final ChooseCardNameAtResolutionEffectHandler chooseCardNameHandler;
    private final AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler attachedCreatureCopyHandler;

    public AttachmentTriggers(EventAbilitySupport abilities, @Lazy GameQueryService gameQueryService,
                              @Lazy PlayerInputService playerInputService,
                              @Lazy TriggeredAbilityQueueService triggeredAbilityQueueService,
                              @Lazy ChooseCardNameAtResolutionEffectHandler chooseCardNameHandler,
                              @Lazy AttachedCreatureBecomesCopyOfExiledCreatureEffectHandler attachedCreatureCopyHandler) {
        this.abilities = abilities;
        this.gameQueryService = gameQueryService;
        this.playerInputService = playerInputService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.chooseCardNameHandler = chooseCardNameHandler;
        this.attachedCreatureCopyHandler = attachedCreatureCopyHandler;
    }

    /** Triggers the abilities {@code event} triggers. */
    public void trigger(GameData gameData, AttachmentEvent event) {
        switch (event) {
            case AttachmentEvent.AuraAttached attached -> auraAttached(gameData, attached.aura(),
                    attached.attachedToId());
            case AttachmentEvent.EquipmentAttached attached -> equipmentAttached(gameData, attached);
            case AttachmentEvent.EquipmentUnattached unattached -> equipmentUnattached(gameData, unattached);
        }
    }

    /**
     * The abilities that see {@code aura} become attached to {@code attachedToId}: a permanent's "an Aura becomes
     * attached to this" (when {@code aura} is an Aura card), recording the enchanted permanent as it was; Siona's, the
     * Aura as the triggering permanent ("that Aura") with its controller; Eriette's, the Aura as the triggering
     * permanent and the enchanted permanent as the non-targeting "that permanent". An Aura attached to a player
     * triggers none of them.
     */
    private void auraAttached(GameData gameData, Permanent aura, UUID attachedToId) {
        if (attachedToId == null) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, attachedToId);
        if (enchanted == null) {
            return;
        }
        Card auraCard = aura.getCard();
        UUID enchantedControllerId = gameQueryService.findPermanentController(gameData, attachedToId);
        if (auraCard.getSubtypes().contains(CardSubtype.AURA) && enchantedControllerId != null) {
            for (TriggeredAbility ability : abilities.abilities(gameData, enchanted, AURA_TO_THIS)) {
                abilities.put(gameData, ability, enchanted.getCard(), enchanted, enchantedControllerId,
                        ConditionContext.forPermanent(enchanted, enchantedControllerId),
                        EventAbilitySupport.eventRecord(enchanted, null, null, null, null, null));
            }
        }
        UUID auraControllerId = gameQueryService.findPermanentController(gameData, aura.getId());
        if (auraControllerId == null) {
            return;
        }
        if (auraControllerId.equals(enchantedControllerId) && gameQueryService.isCreature(gameData, enchanted)) {
            for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(auraControllerId,
                    List.of()))) {
                for (TriggeredAbility ability : abilities.abilities(gameData, watcher, AURA_TO_YOUR_CREATURE)) {
                    abilities.put(gameData, ability, watcher.getCard(), watcher, auraControllerId,
                            ConditionContext.forPermanent(watcher, auraControllerId),
                            EventAbilitySupport.eventRecord(watcher, null, null, null, aura, auraControllerId));
                }
            }
        }
        if (auraControllerId.equals(enchantedControllerId) || gameQueryService.isLand(gameData, enchanted)
                || enchanted.getCard().getManaValue() > auraCard.getManaValue()) {
            return;
        }
        for (Permanent watcher : List.copyOf(gameData.playerBattlefields.getOrDefault(auraControllerId, List.of()))) {
            for (TriggeredAbility ability : abilities.abilities(gameData, watcher,
                    AURA_TO_OPPONENT_NONLAND_PERMANENT)) {
                abilities.put(gameData, ability, watcher.getCard(), watcher, auraControllerId,
                        ConditionContext.forPermanent(watcher, auraControllerId),
                        EventAbilitySupport.eventRecord(watcher, enchanted.getId(), null, null, aura,
                                auraControllerId));
            }
        }
    }

    /**
     * The Equipment's "whenever this Equipment becomes attached to a creature": the record names the creature as the
     * triggering permanent with the Equipment's controller, and the Equipment as it was.
     */
    private void equipmentAttached(GameData gameData, AttachmentEvent.EquipmentAttached event) {
        Permanent equipment = event.equipment();
        Permanent equipped = gameQueryService.findPermanentById(gameData, event.equippedId());
        if (equipped == null) {
            return;
        }
        UUID controllerId = event.controllerId();
        for (TriggeredAbility ability : abilities.abilities(gameData, equipment, EQUIPMENT_ATTACHED)) {
            abilities.put(gameData, ability, equipment.getCard(), equipment, controllerId,
                    ConditionContext.forPermanent(equipment, controllerId),
                    EventAbilitySupport.eventRecord(equipment, null, null, null, equipped, controllerId));
        }
    }

    /**
     * The Equipment's "whenever this Equipment becomes unattached from a permanent": the record names the permanent it
     * left as the triggering permanent ("that permanent"), and the Equipment as it was.
     */
    private void equipmentUnattached(GameData gameData, AttachmentEvent.EquipmentUnattached event) {
        Permanent equipment = event.equipment();
        UUID controllerId = event.controllerId();
        UUID unattachedFromId = event.unattachedFromId();
        for (TriggeredAbility ability : abilities.abilities(gameData, equipment, EQUIPMENT_UNATTACHED)) {
            Permanent snapshot = new Permanent(equipment);
            boolean triggered = abilities.put(gameData, ability, equipment.getCard(), equipment, controllerId,
                    ConditionContext.forPermanent(equipment, controllerId), entry -> {
                        entry.setNonTargeting(true);
                        entry.setTriggeringPermanentId(unattachedFromId);
                        entry.setSourcePermanentSnapshot(snapshot);
                    });
            if (triggered) {
                log.info("Game {} - {} triggers on becoming unattached", gameData.id, equipment.getCard().getName());
            }
        }
    }

    /**
     * After {@code equipment} became attached to a creature: its "as this Equipment becomes attached to a creature"
     * abilities apply while it becomes attached, without using the stack ({@link AsThisBecomesAttachedEffect}: a color,
     * a creature card name and a creature type, a copy of a creature card exiled with it), and then it is an
     * {@link AttachmentEvent.EquipmentAttached} event, which its "whenever this Equipment becomes attached to a
     * creature" abilities trigger on.
     */
    public void equipmentBecameAttached(GameData gameData, Permanent equipment, UUID oldAttachedTo) {
        if (equipment == null || equipment.getAttachedTo() == null
                || equipment.getAttachedTo().equals(oldAttachedTo)
                || !equipment.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            return;
        }

        Permanent equipped = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (equipped == null || !gameQueryService.isCreature(gameData, equipped)) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, equipment.getId());
        if (controllerId == null) return;

        for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, equipment)) {
            if (effect instanceof AsThisBecomesAttachedEffect asAttaches) {
                applyAsBecomesAttached(gameData, equipment, equipped, controllerId, asAttaches.effect());
            }
        }

        gameData.rulesEvents.occur(new AttachmentEvent.EquipmentAttached(equipment, equipped.getId(), controllerId));
        triggeredAbilityQueueService.beginStackDoorChoices(gameData);
    }

    /**
     * Applies {@code effect}, an "as this Equipment becomes attached to a creature" ability of {@code equipment}, as it
     * becomes attached to {@code equipped}: its controller chooses a color, or a creature card name and then a creature
     * type, for the Equipment; or the creature becomes a copy of a creature card exiled with the Equipment.
     */
    private void applyAsBecomesAttached(GameData gameData, Permanent equipment, Permanent equipped,
                                        UUID controllerId, CardEffect effect) {
        if (effect instanceof ChooseColorForSourceEffect) {
            playerInputService.beginColorChoice(gameData, controllerId, equipment.getId(), null);
        } else if (effect instanceof SequenceEffect sequence && sequence.steps().size() == 2
                && sequence.steps().get(0) instanceof ChooseCardNameAtResolutionEffect nameChoice
                && sequence.steps().get(1) instanceof ChooseSubtypeForSourceEffect subtypeChoice) {
            chooseCardNameHandler.begin(gameData, equipment.getCard(), controllerId, equipment.getId(),
                    nameChoice, subtypeChoice);
        } else if (effect instanceof AttachedCreatureBecomesCopyOfExiledCreatureEffect) {
            // The attachment's record, never put on the stack: the Equipment and the creature it became attached to.
            StackEntry attachment = StackEntry.carrier(equipment.getCard(), controllerId,
                    equipment.getCard().getName() + "'s ability", List.of(effect), null, equipment.getId());
            attachment.setTriggeringPermanentId(equipped.getId());
            attachedCreatureCopyHandler.resolve(gameData, attachment, effect);
        } else {
            throw new IllegalStateException(equipment.getCard().getName()
                    + " has an \"as this becomes attached\" ability nothing applies: " + effect);
        }
    }
}
