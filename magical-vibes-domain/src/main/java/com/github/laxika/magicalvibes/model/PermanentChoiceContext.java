package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndReturnTargetCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



public sealed interface PermanentChoiceContext extends PendingInteraction {

    record CloneCopy() implements PermanentChoiceContext {}

    record CipherEncode() implements PermanentChoiceContext {}

    record AuraGraft(UUID auraPermanentId) implements PermanentChoiceContext {}

    /** Glamer Spinners: move every Aura in {@code auraPermanentIds} onto the chosen permanent. */
    record AttachAllAurasToAnotherPermanent(List<UUID> auraPermanentIds) implements PermanentChoiceContext {}

    /** Stonehewer Giant: attach the just-placed Equipment {@code equipmentPermanentId} to the chosen creature. */
    record AttachEquipmentToCreature(UUID equipmentPermanentId, UUID controllerId) implements PermanentChoiceContext {}

    /** Nettlevine Blight: sacrifice {@code permanentToSacrificeId}, then reattach the source Aura
     *  {@code auraPermanentId} onto the chosen creature or land. */
    record ReattachSourceAuraAfterSacrifice(UUID auraPermanentId, UUID permanentToSacrificeId) implements PermanentChoiceContext {}

    /** Attach the source Aura to the chosen permanent after a resolving effect pauses for input. */
    record AttachSourceAuraToChosenPermanent(UUID auraPermanentId) implements PermanentChoiceContext {}

    /** Enchantment Alteration: move the targeted Aura to another permanent of the same type. */
    record AttachTargetAuraToAnotherPermanentOfSameType(UUID auraPermanentId) implements PermanentChoiceContext {}

    record LegendRule(String cardName) implements PermanentChoiceContext {}

    record BounceCreature(UUID bouncingPlayerId) implements PermanentChoiceContext {}

    record SpellRetarget(UUID spellCardId) implements PermanentChoiceContext {}

    record PsychicBattleRetarget(UUID spellCardId, UUID controllerId, Card sourceCard, int targetIndex)
            implements PermanentChoiceContext {}

    record SacrificeCreature(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    /** Torment of Hailfire: {@code playerId} sacrifices the chosen nonland permanent they control. */
    record TormentSacrifice(UUID playerId) implements PermanentChoiceContext {}

    /** The chosen creature is destroyed, or exiled instead when {@code exile} is true (Doomfall). */
    record DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName, boolean exile) implements PermanentChoiceContext {
        public DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName) {
            this(choosingPlayerId, sourceCardName, false);
        }
    }

    /** Godsend: choose one creature blocking or blocked by the equipped creature to exile. */
    record ExileCombatOpponent(UUID sourcePermanentId, Card sourceCard) implements PermanentChoiceContext {}

    /** An attack trigger asks the defending player to choose an untapped creature that must block. */
    record DefendingPlayerChoosesCreatureToBlock(UUID choosingPlayerId, UUID sourcePermanentId,
                                                 String sourceCardName) implements PermanentChoiceContext {}

    /**
     * Riches: {@code choosingPlayerId} picks one creature they control; remaining opponents choose
     * next, then {@code gainingControllerId} seizes all {@code accumulatedChosenIds} simultaneously.
     */
    record OpponentChoosesCreatureYouGainControl(
            UUID choosingPlayerId,
            UUID gainingControllerId,
            String sourceCardName,
            List<UUID> remainingOpponentIds,
            List<UUID> accumulatedChosenIds
    ) implements PermanentChoiceContext {}

    /** Goblin Festival: the ability controller chooses which opponent gains control of the source. */
    record ChooseOpponentGainsControlOfSource(UUID sourcePermanentId, String sourceCardName)
            implements PermanentChoiceContext {}

    /**
     * Echo Chamber: {@code choosingPlayerId} picks one creature they control; a token copy of it is
     * then created under {@code copyControllerId}'s control from {@code sourceCard}.
     */
    record OpponentChoosesCreatureTheyControlToCopy(
            UUID choosingPlayerId,
            UUID copyControllerId,
            Card sourceCard
    ) implements PermanentChoiceContext {}

    /**
     * Opponent accepted Infernal Denizen's upkeep may and is picking which creature of
     * {@code victimControllerId}'s to gain control of for {@code duration}, keyed to {@code sourcePermanentId}.
     */
    record OpponentMayGainControlOfCreatureYouControl(
            UUID choosingOpponentId,
            UUID victimControllerId,
            UUID sourcePermanentId,
            String sourceCardName,
            com.github.laxika.magicalvibes.model.effect.ControlDuration duration
    ) implements PermanentChoiceContext {}

    record SacrificeCreatureThenSearchLibrary(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    /** Retribution: {@code sacrificingPlayerId} picks which of the two targeted creatures they
     *  sacrifice; the other one gets a -1/-1 counter from {@code sourceCard}. */
    record SacrificeOneOfTwoThenCounterOnOther(UUID sacrificingPlayerId, Card sourceCard, UUID controllerId,
                                               UUID firstPermanentId, UUID secondPermanentId) implements PermanentChoiceContext {}

    /** Cannibalize: the spell's controller picks which target to exile; the other gets two +1/+1 counters. */
    record CannibalizeChoice(Card sourceCard, UUID controllerId,
                             UUID firstPermanentId, UUID secondPermanentId) implements PermanentChoiceContext {}
    /** Barrin's Spite: the creatures' controller picks which target to sacrifice; the other returns
     *  to its owner's hand. */
    record SacrificeOneOfTwoThenReturnOtherToHand(UUID sacrificingPlayerId, Card sourceCard, UUID controllerId,
                                                  UUID firstPermanentId, UUID secondPermanentId) implements PermanentChoiceContext {}

    record SacrificeCreatureOpponentsLoseLife(UUID sacrificingPlayerId, String sourceCardName) implements PermanentChoiceContext {}

    /**
     * A controller accepted an upkeep "you may sacrifice" choice and is picking which permanent;
     * the sacrifice puts {@code counterType} on {@code sourcePermanentId}.
     */
    record MaySacrificeForCounterOnSource(UUID controllerId, UUID sourcePermanentId, Card sourceCard,
                                          CounterType counterType) implements PermanentChoiceContext {
        public MaySacrificeForCounterOnSource(UUID controllerId, UUID sourcePermanentId, Card sourceCard) {
            this(controllerId, sourcePermanentId, sourceCard, CounterType.PLUS_ONE_PLUS_ONE);
        }
    }

    /**
     * Gargantuan Gorilla: the controller accepted the upkeep "you may sacrifice a Forest" and is
     * picking which one; a snow Forest grants {@code sourcePermanentId} trample until end of turn.
     */
    record GargantuanGorillaSacrificeForest(UUID controllerId, UUID sourcePermanentId, Card sourceCard)
            implements PermanentChoiceContext {}

    /**
     * Desecration Demon: {@code sacrificingPlayerId} accepted the "any opponent may sacrifice a
     * creature" combat trigger and is picking which of their creatures to sacrifice. {@code effect}
     * carries the remaining-opponent queue so the prompt chain resumes after the choice.
     */
    record AnyOpponentSacrificeCreatureForTapAndCounter(
            UUID sacrificingPlayerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect effect)
            implements PermanentChoiceContext {}

    /** Argothian Wurm: the accepting player is picking which land to sacrifice. */
    record AnyPlayerMaySacrificeLandPutSourceOnTop(
            UUID sacrificingPlayerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect effect)
            implements PermanentChoiceContext {}

    record ForcedCostOrElse(UUID controllerId, UUID sourcePermanentId, Card sourceCard,
                            com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect effect) implements PermanentChoiceContext {}

    /** {@code lifeGainerId} is the sacrificing player for Devour Flesh, the controller otherwise. */
    record SacrificeCreatureControllerGainsLifeEqualToToughness(UUID sacrificingPlayerId, UUID lifeGainerId, String sourceCardName) implements PermanentChoiceContext {}

    record ActivatedAbilityCostChoice(UUID activatingPlayerId,
                                      UUID sourcePermanentId,
                                      Integer abilityIndex,
                                      Integer xValue,
                                      UUID targetId,
                                      Zone targetZone,
                                      CardEffect costEffect,
                                      int remaining,
                                      List<UUID> chosenSoFar) implements PermanentChoiceContext {

        /** Permanents already paid toward this cost, for costs whose valid choices depend on prior
         *  picks (e.g. "tap two creatures that share a creature type"). Empty for count-only costs. */
        public ActivatedAbilityCostChoice(UUID activatingPlayerId, UUID sourcePermanentId, Integer abilityIndex,
                                          Integer xValue, UUID targetId, Zone targetZone, CardEffect costEffect,
                                          int remaining) {
            this(activatingPlayerId, sourcePermanentId, abilityIndex, xValue, targetId, targetZone, costEffect, remaining, List.of());
        }
    }

    /** An activated ability whose second target is chosen by the opponent controlling the first target. */
    record ActivatedAbilityOpponentTarget(UUID activatingPlayerId,
                                          UUID choosingPlayerId,
                                          UUID sourcePermanentId,
                                          Integer abilityIndex,
                                          Integer xValue,
                                          UUID firstTargetId,
                                          Zone targetZone) implements PermanentChoiceContext {}

    /**
     * Targeted death trigger awaiting its target choice (CR 603.3d).
     *
     * @param eventValue numeric payload of the death event, snapshotted onto the stack entry so an
     *                   effect whose amount is an {@code EventValue} can read it at resolution
     *                   (Death's Presence — "X is the power of the creature that died"). {@code null}
     *                   when the trigger carries no such value.
     * @param sourcePermanentSnapshot last-known information for the permanent whose ability
     *                                triggered, when the effect reads that permanent after it left
     *                                the battlefield; {@code null} otherwise.
     */
    record DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                              Integer eventValue, Permanent sourcePermanentSnapshot) implements PermanentChoiceContext {

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects) {
            this(dyingCard, controllerId, effects, null, null);
        }

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                                  Integer eventValue) {
            this(dyingCard, controllerId, effects, eventValue, null);
        }
    }

    /** Targeted ability whose source permanent triggered, with the target chosen as it is put on the stack. */
    record SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      String eventDescription, UUID sourcePermanentId) implements PermanentChoiceContext {
        public SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, "leaves-the-battlefield", null);
        }

        public SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          String eventDescription) {
            this(sourceCard, controllerId, effects, eventDescription, null);
        }
    }

    record DiscardTriggerAnyTarget(Card discardedCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, Permanent sourcePermanentSnapshot,
                                   int eventValue) implements PermanentChoiceContext {
        public MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       UUID sourcePermanentId, Permanent sourcePermanentSnapshot) {
            this(sourceCard, controllerId, effects, sourcePermanentId, sourcePermanentSnapshot, 0);
        }

        public MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, null, null, 0);
        }
    }

    /** "Prevent all damage [the chosen source] would deal this turn" — to the controller only
     *  (Auriok Replica) or to everything (Burrenton Forge-Tender). The legal source choices are
     *  already filtered when the choice begins. */
    record PreventDamageSourceChoice(UUID controllerId, boolean controllerOnly,
                                     boolean gainLifeForBlackOrRedSource) implements PermanentChoiceContext {
        public PreventDamageSourceChoice(UUID controllerId, boolean controllerOnly) {
            this(controllerId, controllerOnly, false);
        }
    }

    record RedirectDamageSourceChoice(UUID controllerId, int amount, UUID redirectTargetId) implements PermanentChoiceContext {}

    /** "All damage that would be dealt to target creature this turn by a source of your choice is dealt to
     *  this creature instead." Chooses the source permanent; {@code protectedCreatureId} is the ability's
     *  target and {@code redirectTargetId} is where redirected damage goes (Oracle's Attendants). When
     *  {@code nextEventOnly} is true, only the next single damage event from the chosen source is
     *  redirected before the shield is consumed (Jade Monolith); otherwise all such damage this turn. */
    record RedirectCreatureDamageSourceChoice(UUID controllerId, UUID protectedCreatureId, UUID redirectTargetId,
                                              boolean nextEventOnly) implements PermanentChoiceContext {}

    record PreventDamageToTargetFromSourceChoice(UUID controllerId, int amount, UUID targetId) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to you this turn, prevent that damage."
     *  Any-color source. When {@code gainLife} is true the controller also gains life equal to the
     *  damage prevented (Reverse Damage); when false there is no life gain (Pentagram of the Ages).
     *  When {@code exileFromLibrary} is true the controller instead exiles that many cards from the
     *  top of their library (Bone Mask). */
    record PreventNextDamageFromSourceChoice(UUID controllerId, boolean gainLife,
                                             boolean exileFromLibrary,
                                             Card damageSourceControllerCard) implements PermanentChoiceContext {
        public PreventNextDamageFromSourceChoice(UUID controllerId, boolean gainLife,
                                                 boolean exileFromLibrary) {
            this(controllerId, gainLife, exileFromLibrary, null);
        }
    }

    /** "The next time a source of your choice would deal damage to any target this turn, prevent that
     *  damage." (Sanctum Guardian). Protects any recipient, not just the controller. When
     *  {@code damageRedSourceController} is true, prevented red damage is dealt back to the source's
     *  controller by {@code passageCard} (Honorable Passage). */
    record PreventNextDamageFromSourceToAnyTargetChoice(UUID controllerId, boolean damageRedSourceController,
                                                        Card passageCard) implements PermanentChoiceContext {
        /** Sanctum Guardian / Circle of Despair: prevention only. */
        public PreventNextDamageFromSourceToAnyTargetChoice(UUID controllerId) {
            this(controllerId, false, null);
        }
    }

    /** "Choose a source you control and flip a coin. If you win the flip, the next time that source
     *  would deal damage this turn, it deals double that damage instead. If you lose the flip, the next
     *  time it would deal damage this turn, prevent that damage." (Desperate Gambit). The coin is
     *  flipped once the source has been chosen. */
    record DoubleOrPreventNextDamageFromSourceChoice(UUID controllerId) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to enchanted creature this turn,
     *  prevent that damage." (Kithkin Armor). {@code protectedPermanentId} is the creature the
     *  sacrificed Aura was attached to; only damage dealt to it consumes the shield. */
    record PreventNextDamageFromSourceToPermanentChoice(UUID controllerId,
                                                        UUID protectedPermanentId) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to you and/or creatures you control
     *  this turn, prevent that damage. If damage from a black source is prevented this way, you gain
     *  that much life." (Shadowbane). */
    record PreventNextDamageFromSourceToYouAndYourCreaturesChoice(UUID controllerId) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to you this turn, instead that source
     *  deals that much damage to you and Eye for an Eye deals that much damage to that source's
     *  controller." (Eye for an Eye). */
    record EyeForAnEyeSourceChoice(UUID controllerId, Card eyeCard) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage this turn, that damage is dealt to
     *  that source's controller instead." (Reflect Damage). */
    record ReflectDamageToSourceControllerChoice(UUID controllerId) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage this turn, that damage is dealt to
     *  this creature instead." (Opal-Eye, Konda's Yojimbo). */
    record RedirectNextDamageFromChosenSourceToPermanentChoice(UUID controllerId, UUID destinationPermanentId)
            implements PermanentChoiceContext {}

    record AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Meandering Towershell: choose the opponent or opposing planeswalker it attacks on return. */
    record ExileReturnAttackTarget(PendingExileReturn pending, List<PendingExileReturn> remaining)
            implements PermanentChoiceContext {

        public ExileReturnAttackTarget {
            remaining = List.copyOf(remaining);
        }
    }

    /** Decimator Beetle attack trigger, stage 1: choose the creature you control to remove a counter
     *  from. Only this stage is parked on the pending-interaction queue; stage 2 is begun directly by
     *  the stage-1 handler. {@code defendingPlayerId} is the player whose creatures are legal stage-2
     *  targets (null when the attack has no defending player). */
    record AttackCounterMoveFirstTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                        UUID sourcePermanentId, UUID defendingPlayerId) implements PermanentChoiceContext {}

    /** Decimator Beetle attack trigger, stage 2: choose up to one creature the defending player
     *  controls to put a counter on. Choosing {@code controllerId} means "no second target".
     *  {@code firstTargetId} is the stage-1 choice. */
    record AttackCounterMoveSecondTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, UUID defendingPlayerId, UUID firstTargetId) implements PermanentChoiceContext {}

    /** Targeted "whenever a permanent enters" trigger (e.g. Reaper King — "Whenever another Scarecrow
     *  you control enters, destroy target permanent."). The controller chooses the target when the
     *  enter trigger is serviced; mirrors {@link AttackTriggerTarget}'s any-permanent target flow.
     *  {@code enteringPermanentId} is the permanent whose entry caused the trigger; it becomes the
     *  stack entry's {@code triggeringPermanentId} so an effect that acts on "that creature"
     *  (Gruul Ragebeast's fight) can find it. {@code null} when the effect only needs the source. */
    record EntersTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId,
                               UUID enteringPermanentId, UUID targetSourcePermanentId, TargetFilter targetFilter) implements PermanentChoiceContext {

        public EntersTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null, null, null);
        }

        public EntersTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, UUID enteringPermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, enteringPermanentId, null, null);
        }

        public EntersTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, UUID enteringPermanentId,
                                   UUID targetSourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, enteringPermanentId,
                    targetSourcePermanentId, null);
        }
    }

    record TriggeredModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                 UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Targeted "whenever you cycle or discard a card" trigger on a battlefield permanent
     *  ({@code EffectSlot.ON_CONTROLLER_DISCARDS}), e.g. Zenith Seeker — "target creature gains
     *  flying until end of turn." The controller chooses the target when the discard trigger is
     *  serviced; mirrors {@link EntersTriggerTarget}'s any-permanent target flow. */
    record DiscardControllerTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       boolean playerTargetOnly, TargetFilter targetFilter,
                                       int spellManaSpentX, UUID sourcePermanentId,
                                       Permanent sourcePermanentSnapshot, boolean optionalTarget,
                                       UUID triggeringPermanentId)
            implements PermanentChoiceContext {

        /** Convenience constructor for any-target (permanents + players). */
        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, false, null, 0, null, null, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly) {
            this(sourceCard, controllerId, effects, playerTargetOnly, null, 0, null, null, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter, 0, null, null, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, null, null, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           Permanent sourcePermanentSnapshot) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, sourcePermanentSnapshot, false, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           boolean optionalTarget) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, optionalTarget, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           UUID triggeringPermanentId) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, false, triggeringPermanentId);
        }
    }

    record BounceOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

    /** "Sacrifice this permanent unless you sacrifice a [permanent]." The chosen permanent is sacrificed. (Sacred Mesa.) */
    record SacrificeOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

    /** "If this permanent would enter, sacrifice a [permanent] instead. If you do, put it onto the
     *  battlefield. If you don't, put it into its owner's graveyard." (Balduvian Trading Post.) The
     *  entering permanent is parked here — it is in no zone while the choice is pending. Choosing
     *  {@code controllerId} (offered as a player option) declines, sending the card to the graveyard. */
    record SacrificePermanentToEnter(UUID controllerId, Permanent enteringPermanent) implements PermanentChoiceContext {}

    /** Champion a creature: exile the chosen creature until the source permanent leaves the battlefield. */
    record ChampionCreature(UUID sourcePermanentId, UUID controllerId) implements PermanentChoiceContext {}

    /** "Put a creature you control on top of its owner's library." The controller chooses one of their
     *  creatures (the source itself is a legal choice) as the effect resolves. (Nulltread Gargantuan.) */
    record PutControlledCreatureOnTopOfLibrary(UUID controllerId) implements PermanentChoiceContext {}

    /** Pattern Matcher: choose another controlled creature whose name bounds the library search. */
    record PatternMatcherCreatureChoice(UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Polymorphous Rush: choose the creature whose copiable characteristics will be used. */
    record PolymorphousRushCreatureChoice(UUID controllerId,
                                           MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect effect)
            implements PermanentChoiceContext {}

    /** Populate (CR 701.36a): the controller chooses which creature token they control is copied. */
    record Populate(UUID controllerId) implements PermanentChoiceContext {}

    /** Soulbond self-enter: choose another unpaired creature you control to pair with the source. */
    record SoulbondChoosePartner(UUID sourcePermanentId, UUID controllerId) implements PermanentChoiceContext {}

    /** "When a creature is championed with this permanent, [targeted effect]." Chooses the target for a
     *  {@code EffectSlot.ON_CHAMPIONED} triggered ability (e.g. Mistbind Clique — tap all lands target
     *  player controls). Fired mid-resolution when the Faerie is championed. */
    record ChampionedTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record EmblemTriggerTarget(String emblemDescription, UUID controllerId, List<CardEffect> effects, Card sourceCard, boolean opponentControlledOnly) implements PermanentChoiceContext {
        /** Convenience constructor for backwards compatibility (targets any permanent). */
        public EmblemTriggerTarget(String emblemDescription, UUID controllerId, List<CardEffect> effects, Card sourceCard) {
            this(emblemDescription, controllerId, effects, sourceCard, false);
        }
    }

    /** {@code targetFilter} overrides the source card's own filter when the trigger's legal targets
     *  belong to one chosen mode rather than to the whole card (Demonic Pact's "target opponent"). */
    record UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId,
                                     TargetFilter targetFilter, UUID choosingPlayerId) implements PermanentChoiceContext {

        public UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null, controllerId);
        }

        public UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, sourcePermanentId, targetFilter, controllerId);
        }
    }

    /** A player-targeted trigger from a precombat or postcombat main phase. */
    record MainPhasePlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                        UUID sourcePermanentId, TargetFilter targetFilter)
            implements PermanentChoiceContext {}

    record PlayerWithLowestLifeChoice(Card sourceCard) implements PermanentChoiceContext {}

    record LeastToughnessDamageChoice(Card sourceCard, int damage) implements PermanentChoiceContext {}

    record UpkeepMultiPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** {@code targetFilter} overrides the source card's own filter — see {@link UpkeepPlayerTargetTrigger}. */
    record UpkeepAnyTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId,
                                  TargetFilter targetFilter) implements PermanentChoiceContext {

        public UpkeepAnyTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null);
        }
    }

    /**
     * A modal upkeep trigger whose mode must be chosen before its ability is put on the stack. The
     * chosen mode's effects then go through the ordinary upkeep trigger routing.
     */
    record UpkeepModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                              UUID sourcePermanentId, boolean consumeModes) implements PermanentChoiceContext {

        public UpkeepModalTrigger(Card sourceCard, UUID controllerId, ChooseModeNotYetChosenEffect effect,
                                  UUID sourcePermanentId) {
            this(sourceCard, controllerId, new ChooseOneEffect(effect.options()), sourcePermanentId, true);
        }

        public UpkeepModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                  UUID sourcePermanentId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, false);
        }
    }

    record UpkeepPermanentTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                        UUID sourcePermanentId, TargetFilter targetFilter) implements PermanentChoiceContext {

        public UpkeepPermanentTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                            UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null);
        }
    }

    /** "Whenever this permanent phases in, target …" — queued from {@code ON_SELF_PHASES_IN} during
     *  the untap-step phasing action; drained at the start of upkeep when the trigger is put on the
     *  stack. Mirrors {@link UpkeepPermanentTargetTrigger}'s permanent-target flow. */
    record PhasesInTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepSecondPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId, UUID firstTargetPlayerId) implements PermanentChoiceContext {}

    record UpkeepCopyTriggerTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record CapriciousEfreetOwnTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Puca's Mischief step 1: choose the nonland permanent you control. {@code effects} carries the
     *  wrapping {@link com.github.laxika.magicalvibes.model.effect.MayEffect} so it reaches the stack. */
    record PucasMischiefOwnTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Puca's Mischief step 2: choose the opponent's nonland permanent (mana value &le; {@code ownTargetId}'s). */
    record PucasMischiefOpponentTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId, UUID ownTargetId) implements PermanentChoiceContext {}

    record EndStepTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record BeginningOfCombatTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record SacrificeArtifactForDividedDamage(UUID controllerId, Card sourceCard, Map<UUID, Integer> damageAssignments) implements PermanentChoiceContext {}

    /** Heart-Piercer Manticore: choose the creature whose sacrifice creates the reflexive trigger. */
    record SacrificeAnotherCreatureDealPowerDamage(UUID controllerId, Card sourceCard) implements PermanentChoiceContext {}

    record SacrificeAnotherCreatureGainLifeAndDraw(UUID controllerId, Card sourceCard) implements PermanentChoiceContext {}

    record SacrificeCreatureThenMassDamageEqualToPower(UUID controllerId, Card sourceCard) implements PermanentChoiceContext {}

    record ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType,
                                boolean copy, List<UUID> chosenTargets) implements PermanentChoiceContext {
        // {@code copy=true} marks a Paradigm copy that must cease to exist rather than being placed in
        // a zone (CR 707.10a) — both on resolution and when it can't be legally cast. Defaults to false
        // for real cards cast from exile.
        // {@code chosenTargets} accumulates already-selected targets, in the card's declared target
        // order, while a multi-target spell walks its target slots one at a time. Empty for the
        // single-target path (which stores its lone target as the StackEntry's {@code targetId}).
        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType, boolean copy) {
            this(cardToCast, controllerId, spellEffects, spellType, copy, List.of());
        }

        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, false, List.of());
        }
    }

    record ChandraTorchCastSpellTarget(Card cardToCast, UUID controllerId, Card sourceCard, int damage,
                                       List<UUID> chosenTargets) implements PermanentChoiceContext {}

    record GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                    StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                    boolean withoutPayingManaCost, UUID ownerId) implements PermanentChoiceContext {

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                        boolean withoutPayingManaCost) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard,
                    withoutPayingManaCost, null);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, false, true, null);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard, true);
        }
    }

    /**
     * A spell cast from hand that still needs its target chosen. {@code xValue} is the X announced
     * while casting (CR 601.2b) and must ride onto the stack entry built after the target choice —
     * a targeted alternative-cost X spell such as Bonfire of the Damned would otherwise resolve
     * with X=0.
     */
    record HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                               StackEntryType spellType, int xValue) implements PermanentChoiceContext {

        public HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, 0);
        }
    }

    record ChooseCreatureAsEnter(UUID enteringPermanentId, UUID controllerId, Card card, UUID targetId,
                                 boolean wasCastFromHand, int etbMode, boolean kicked) implements PermanentChoiceContext {}

    record LifeGainTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                    UUID sourcePermanentId, boolean creaturesOnly) implements PermanentChoiceContext {
        /** Any-target (creature or player) life-gain trigger — the historical Firesong/Sunspeaker form. */
        public LifeGainTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, false);
        }
    }

    /** "Whenever you draw a card, [source] deals damage to any target." Queued when a controller-draw
     *  trigger carries an any-target effect (e.g. Niv-Mizzet, the Firemind); the controller chooses a
     *  creature or player before the triggered ability goes on the stack. */
    record DrawTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** An enters-the-battlefield trigger that needs an "any target" choice and whose effect resolves
     *  against the permanent that just entered rather than the triggering permanent — e.g. "that creature
     *  deals damage equal to its power to any target". The {@code sourcePermanentId} points at the
     *  permanent that entered (the damage source); {@code sourceCard} is the permanent whose ability
     *  triggered (Flayer of the Hatebound, Warstorm Surge). */
    record EnteringPermanentAnyTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /**
     * ETB trigger that needs to target a spell on the stack (e.g. Naru Meha's copy ability).
     * {@code includeAbilities} is true when the card's stack filter includes
     * {@code StackEntryHasTargetPredicate}, so abilities on the stack are legal targets too
     * ("target spell or ability" — Mizzium Meddler). {@code sourcePermanentId} is the permanent
     * that just entered, for effects that act on the source permanent (target redirection).
     */
    record ETBSpellTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 StackEntryPredicate spellFilter, boolean includeAbilities,
                                 UUID sourcePermanentId) implements PermanentChoiceContext {}

    /**
     * Exploit sacrifice choice: controller picks any creature they control (including the exploit
     * source) to sacrifice. {@code sourceStillOnBattlefield} gates whether {@code ON_EXPLOIT}
     * fires after the sacrifice (false when the exploit permanent left before resolution).
     */
    record ExploitSacrifice(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                            boolean sourceStillOnBattlefield) implements PermanentChoiceContext {}

    /**
     * "When this creature exploits a creature" trigger that needs a stack target (spell and/or
     * ability). {@code includeAbilities} is true when the card's stack filter includes
     * {@code StackEntryHasTargetPredicate} (Overcharged Amalgam).
     */
    record ExploitTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                UUID sourcePermanentId, StackEntryPredicate stackFilter,
                                boolean includeAbilities) implements PermanentChoiceContext {}

    /**
     * ETB trigger on a token copy that needs to choose a target at trigger time (CR 603.3).
     * Used when a token copy is created of a creature with a targeted ETB ability
     * (e.g. Cackling Counterpart → Homarid Explorer). The target can't be chosen at cast
     * time because the token wasn't cast — it's created directly on the battlefield.
     */
    record ETBTokenTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 UUID sourcePermanentId, TargetFilter targetFilter) implements PermanentChoiceContext {}

    /**
     * Multi-target trigger for creatures with multiple target groups or groups with
     * {@code maxTargets > 1} (e.g. Burning Sun's Avatar ETB; Elder Deep-Fiend ON_SELF_CAST
     * "tap up to four target permanents"). Targets are chosen slot-by-slot at trigger time:
     * each group can accept up to {@code maxTargets} targets before advancing. Chosen targets
     * accumulate in {@code chosenTargetsSoFar}. A response equal to {@code controllerId}
     * signals "done with this group" — only valid once the group's minimum has been met.
     * {@code sourcePermanentId} is null for cast-time (ON_SELF_CAST) triggers.
     */
    /**
     * Slot-by-slot target walk shared by ETB / self-cast / attack / beginning-of-combat triggers.
     *
     * <p>{@code groupSizes} records how many targets each already-finished group actually took, so a
     * declined "up to one" group contributes a 0 rather than silently shifting the later groups'
     * slices of the flat target list (see {@code StackEntry.targetsForGroup}). The legacy constructor
     * leaves it empty, which keeps the positional {@code maxTargets} slicing.</p>
     */
    record ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                      int currentGroupIndex, int chosenInCurrentGroup,
                                      List<Integer> groupSizes, int xValue,
                                      List<String> repeatedAdditionalCosts,
                                      boolean resumePendingMayResolution) implements PermanentChoiceContext {

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, List.of(), 0, List.of(), false);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, 0, List.of(), false);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue, List.of(), false);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue,
                                          boolean resumePendingMayResolution) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue, List.of(),
                    resumePendingMayResolution);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue,
                                          List<String> repeatedAdditionalCosts) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue,
                    repeatedAdditionalCosts, false);
        }
    }

    /** Saga chapter ability that targets a permanent (e.g. Phyrexian Scriptures chapter I).
     *  {@code targetFilters} restricts valid targets (e.g. "creature an opponent controls"); null/empty = any creature. */
    record SagaChapterTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                             UUID sourcePermanentId, String chapterName,
                             Set<TargetFilter> targetFilters) implements PermanentChoiceContext {}

    /** Saga chapter ability that targets a card in a graveyard (e.g. The Mirari Conjecture chapters I/II). */
    record SagaChapterGraveyardTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, String chapterName) implements PermanentChoiceContext {}

    record GraveyardAbilityCostChoice(UUID activatingPlayerId,
                                      Card graveyardCard,
                                      int graveyardCardIndex,
                                      Integer abilityIndex,
                                      CardEffect costEffect,
                                      int remaining,
                                      List<UUID> chosenSoFar) implements PermanentChoiceContext {

        /** Permanents already paid toward this cost, for sequential multi-slot costs
         *  (e.g. "Sacrifice a Swamp and a Forest"). Empty for count-only costs. */
        public GraveyardAbilityCostChoice(UUID activatingPlayerId, Card graveyardCard, int graveyardCardIndex,
                                          Integer abilityIndex, CardEffect costEffect, int remaining) {
            this(activatingPlayerId, graveyardCard, graveyardCardIndex, abilityIndex, costEffect, remaining, List.of());
        }
    }

    /** Tap-cost payment for a resolution-time may ability (e.g. Aziza, Mage Tower Captain). */
    record MayAbilityTapCostChoice(UUID playerId,
                                   UUID sourcePermanentId,
                                   com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost costEffect,
                                   int remaining,
                                   PendingMayAbility mayAbility) implements PermanentChoiceContext {}

    /**
     * Spell-cast trigger that needs to target a card in a graveyard (e.g. Teshar, Ancestor's Apostle).
     * {@code graveyardOwnerId} narrows the searched graveyards to a single player — "target creature
     * card from <em>that player's</em> graveyard" (Ink-Eyes, Servant of Oni, where "that player" is
     * the one the source dealt combat damage to). {@code null} keeps the effect's own
     * {@code GraveyardSearchScope}.
     */
    record SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       UUID graveyardOwnerId, int minCount) implements PermanentChoiceContext {

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, null, 0);
        }

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID graveyardOwnerId) {
            this(sourceCard, controllerId, effects, graveyardOwnerId, 0);
        }
    }

    /** "Sacrifice a [permanent]. If you do, [effect]." (e.g. The First Eruption chapter III). */
    record SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect) implements PermanentChoiceContext {}

    /** Victimize: sacrifice a creature, then return the selected graveyard cards if the sacrifice happened. */
    record SacrificePermanentAndReturnTargetCards(UUID controllerId, Card sourceCard,
                                                  SacrificePermanentAndReturnTargetCardsFromGraveyardEffect effect)
            implements PermanentChoiceContext {}

    /** "Sacrifice another permanent. If you do, this creature gets +X/+Y." */
    record SacrificePermanentAndBoostSelf(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                          int power, int toughness, String permanentDescription) implements PermanentChoiceContext {}

    /** "Sacrifice another permanent. If you do, this creature gains [keyword]." */
    record SacrificePermanentAndGrantKeywordSelf(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                                 Set<Keyword> keywords, String permanentDescription)
            implements PermanentChoiceContext {}

    /** "Blight N. If you do, [effect]." */
    record BlightCreatureChoice(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                com.github.laxika.magicalvibes.model.effect.BlightEffect effect)
            implements PermanentChoiceContext {}

    /** Each opponent chooses a creature they control for a mandatory blight action. */
    record EachOpponentBlightsCreature(
            UUID choosingPlayerId,
            UUID sourceControllerId,
            Card sourceCard,
            UUID sourcePermanentId,
            List<UUID> remainingOpponentIds,
            int count
    ) implements PermanentChoiceContext {}

    /** "Sacrifice a creature. If you do, create X tokens, where X is its toughness." (e.g. Feed the Pack). */
    record SacrificeCreatureCreateTokensEqualToToughness(UUID controllerId, Card sourceCard,
                                                         com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate) implements PermanentChoiceContext {}

    /** "Sacrifice a creature. If you do, create one token whose power and toughness are each equal to
     *  the sacrificed creature's power." (e.g. Ooze Garden). */
    record SacrificeCreatureCreateSizedTokenEqualToPower(UUID controllerId, Card sourceCard,
                                                         com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate) implements PermanentChoiceContext {}

    /** "Target player sacrifices a creature of their choice. If a [subtype] is sacrificed this way,
     *  that player creates [tokens]." (Warren Weirding.) The sacrificing player also creates the tokens. */
    record SacrificeCreatureCreateTokensIfSubtype(UUID sacrificingPlayerId, Card sourceCard,
                                                  CardSubtype requiredSubtype,
                                                  com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenTemplate) implements PermanentChoiceContext {}

    /** Explore trigger that needs to target a creature an opponent controls
     *  (e.g. Lurking Chupacabra: "Whenever a creature you control explores, target creature
     *  an opponent controls gets -2/-2 until end of turn."). */
    record ExploreTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Clash trigger ({@code EffectSlot.ON_CONTROLLER_CLASHES}) that needs to target a creature an
     *  opponent controls (e.g. Entangling Trap: "Whenever you clash, tap target creature an opponent
     *  controls. If you won, ..."). The {@code effects} have already been resolved for the clash
     *  outcome (win-conditional effects included only on a won clash). */
    record ClashTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Transform trigger that first chooses a target opponent, then up to one creature that player controls. */
    record TransformOpponentThenCreatureTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                               UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Creature choices for TransformOpponentThenCreatureTarget. Choosing controllerId means no more creature targets. */
    record TransformCreatureTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, UUID opponentId, List<UUID> creatureIds,
                                   int maxCreatureTargets) implements PermanentChoiceContext {}

    /** A transform trigger whose mandatory effect needs a target chosen as the trigger is put on the stack. */
    record TransformTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                  UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Valleymaker's mana ability ("Choose a player. That player adds {G}{G}{G}."). The activating
     *  player picks the recipient; {@code amount} mana of {@code color} is added to that player's pool
     *  (tracking creature mana when {@code creatureSource}). Begun inline during mana-ability resolution. */
    record ManaAbilityAddToChosenPlayer(ManaColor color, int amount, boolean creatureSource,
                                        String sourceCardName) implements PermanentChoiceContext {}

    /** Bend or Break: a player chooses which opponent will choose one of their land piles. */
    record BendOrBreakOpponentChoice(UUID playerId) implements PermanentChoiceContext {}

    /** Curator of Destinies: the controller chooses which opponent chooses between the two piles. */
    record CuratorOpponentChoice() implements PermanentChoiceContext {}

    /** Tariff tie-break: {@code playerId} chooses which of their creatures tied for greatest mana
     *  value is the one they must pay for or sacrifice. */
    record TariffTieBreak(UUID playerId, Card sourceCard) implements PermanentChoiceContext {}

    /** Juxtapose tie-break: a player chooses which of their permanents tied for greatest mana value
     *  participates in the exchange. {@code artifactPhase} distinguishes the creature step from the
     *  artifact step. While {@code controllerChosen} is false the pending choice belongs to the spell's
     *  controller; once true, {@code controllerPermanentId} holds the controller's already-selected
     *  permanent and the pending choice belongs to the target player. */
    record JuxtaposeTieBreak(Card sourceCard, UUID controllerId, UUID targetPlayerId,
                             boolean artifactPhase, boolean controllerChosen,
                             UUID controllerPermanentId) implements PermanentChoiceContext {}

}
