package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old becomes-the-target slot collectors reads a triggered ability, now that the ability has a real
 * trigger (effect-slot-refactor.md step 13, family 6). Each constant is named after the slot whose collector it stands
 * for and names the trigger its cards were converted to and the zone the collector reads them in. The collectors in
 * {@code TriggerCollectionService} still decide when the ability triggers, how its effects group into stack objects,
 * which wrappers filter it and which event data it is bound to, as the slot's did, until steps 14 and 15 of the
 * refactor replace them with trigger options and effects that read the event.
 * <p>
 * The collectors read the abilities in the form the cards wrote them before step 14
 * ({@link Card#getLegacyTriggerEffects(Trigger, Zone)}): a limit wrapped around each effect, a target trigger's filter
 * as the {@code TriggeringPermanentConditionalEffect} they judge, "this becomes the target of a spell [or ability] you
 * control" as the {@code TriggeringSpellControllerConditionalEffect} and "a player or permanent becomes the target of
 * an ability you control" as the {@code AbilityTargetChoiceTriggerEffect} the collectors of the events about anyone
 * judge.
 */
public enum TargetWatch implements TriggerWatch {
    /** "Whenever a player chooses one or more targets" for a spell or ability. */
    ON_ANY_PLAYER_CHOOSES_TARGETS(Triggers.playerChoosesTargets(), Zone.BATTLEFIELD),
    /** "Whenever this permanent becomes the target of a spell". */
    ON_BECOMES_TARGET_OF_SPELL(Triggers.becomesTargetOfSpell(), Zone.BATTLEFIELD),
    /** "Whenever this permanent becomes the target of an Aura spell". */
    ON_BECOMES_TARGET_OF_AURA_SPELL(Triggers.becomesTargetOfAuraSpell(), Zone.BATTLEFIELD),
    /** "Whenever this permanent becomes the target of a spell or ability an opponent controls" (ward). */
    ON_BECOMES_TARGET_OF_OPPONENT_SPELL(Triggers.becomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever this permanent becomes the target of a spell an opponent controls". */
    ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY(Triggers.becomesTargetOfOpponentSpell(), Zone.BATTLEFIELD),
    /** "Whenever you become the target of a spell". */
    ON_CONTROLLER_BECOMES_TARGET_OF_SPELL(Triggers.youBecomeTargetOfSpell(), Zone.BATTLEFIELD),
    /** "Whenever you become the target of a spell or ability an opponent controls". */
    ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY(
            Triggers.youBecomeTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever this permanent becomes the target of a spell or ability". */
    ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY(Triggers.becomesTargetOfSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes the target of a spell or ability an opponent controls". */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY(
            Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever another creature you control becomes the target of a spell or ability". */
    ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY(
            Triggers.anotherCreatureYouControlBecomesTargetOfSpellOrAbility(), Zone.BATTLEFIELD),
    /**
     * "Whenever one or more creatures you control become the target of an activated ability", once for each ability.
     */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_ACTIVATED_ABILITY(
            Triggers.creaturesYouControlBecomeTargetOfActivatedAbility(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes the target of a spell an opponent controls". */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL(
            Triggers.creatureYouControlBecomesTargetOfOpponentSpell(), Zone.BATTLEFIELD),
    /**
     * "Whenever a creature you control or a creature spell you control becomes the target of a spell or ability an
     * opponent controls".
     */
    ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY(
            Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever you or a permanent you control becomes the target of a spell or ability an opponent controls". */
    ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY(
            Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever a permanent you control becomes the target of a spell or ability an opponent controls". */
    ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY(
            Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever another permanent you control becomes the target of a spell or ability an opponent controls". */
    ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY(
            Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever a creature becomes the target of a spell or ability". */
    ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY(
            Triggers.creatureBecomesTargetOfSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever a creature an opponent controls becomes the target of a spell or ability you control". */
    ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY(
            Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes the target of a spell". */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL(Triggers.creatureYouControlBecomesTargetOfSpell(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes the target of a backup ability". */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY(
            Triggers.creatureYouControlBecomesTargetOfBackupAbility(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes the target of an instant or sorcery spell". */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY(
            Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    TargetWatch(Trigger trigger, Zone zone) {
        this.trigger = trigger;
        this.zone = zone;
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }

    @Override
    public Zone zone() {
        return zone;
    }

    @Override
    public List<CardEffect> effectsOf(Card card) {
        return card.getLegacyTriggerEffects(trigger, zone);
    }

    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        return card.getLegacyTriggerEffectRegistrations(trigger, zone);
    }
}
