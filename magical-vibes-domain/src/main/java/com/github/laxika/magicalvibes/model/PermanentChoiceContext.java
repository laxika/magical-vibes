package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndReturnTargetCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDrawAndMayPutPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayReturnPermanentToHandAndEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



public sealed interface PermanentChoiceContext extends PendingInteraction {

    record CloneCopy() implements PermanentChoiceContext {}

    record CopyPermanentTargetedBySpell() implements PermanentChoiceContext {}
    record TurnFaceUpCopy(UUID sourcePermanentId, UUID controllerId) implements PermanentChoiceContext {}

    record CipherEncode() implements PermanentChoiceContext {}

    record AuraGraft(UUID auraPermanentId) implements PermanentChoiceContext {}

    /** Glamer Spinners: move every Aura in {@code auraPermanentIds} onto the chosen permanent. */
    record AttachAllAurasToAnotherPermanent(List<UUID> auraPermanentIds) implements PermanentChoiceContext {}

    /** Stonehewer Giant: attach the just-placed Equipment {@code equipmentPermanentId} to the chosen creature. */
    record AttachEquipmentToCreature(UUID equipmentPermanentId, UUID controllerId) implements PermanentChoiceContext {}

    record AttachSacrificedEquipmentToTarget(UUID targetCreatureId, List<UUID> equipmentPermanentIds)
            implements PermanentChoiceContext {
        public AttachSacrificedEquipmentToTarget {
            equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
        }
    }

    record AttachControlledEquipmentToTargetCreature(UUID targetCreatureId, UUID controllerId,
                                                     Card sourceCard, List<UUID> equipmentPermanentIds)
            implements PermanentChoiceContext {
        public AttachControlledEquipmentToTargetCreature {
            equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
        }
    }

    record AttachEquipmentToSamurai(List<UUID> equipmentPermanentIds)
            implements PermanentChoiceContext {
        public AttachEquipmentToSamurai {
            equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
        }
    }

    record AttachEquipmentToSamuraiTarget(UUID samuraiPermanentId, List<UUID> equipmentPermanentIds)
            implements PermanentChoiceContext {
        public AttachEquipmentToSamuraiTarget {
            equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
        }
    }

    /** Reckless Crew: choose at most one distinct Equipment for each created token. */
    record CreateTokensAndAttachEquipment(Card sourceCard, UUID controllerId, List<UUID> tokenIds,
                                          int tokenIndex, List<UUID> chosenEquipmentIds)
            implements PermanentChoiceContext {}

    /** Nettlevine Blight: sacrifice {@code permanentToSacrificeId}, then reattach the source Aura
     *  {@code auraPermanentId} onto the chosen creature or land. */
    record ReattachSourceAuraAfterSacrifice(UUID auraPermanentId, UUID permanentToSacrificeId) implements PermanentChoiceContext {}

    /** Attach the source Aura to the chosen permanent after a resolving effect pauses for input. */
    record AttachSourceAuraToChosenPermanent(UUID auraPermanentId) implements PermanentChoiceContext {}

    /** Attach one selected graveyard Aura to the chosen creature and continue the selection. */
    record AttachReturnedAuraToCreature(UUID controllerId, UUID auraCardId,
                                        List<UUID> remainingAuraCardIds)
            implements PermanentChoiceContext {
        public AttachReturnedAuraToCreature {
            remainingAuraCardIds = List.copyOf(remainingAuraCardIds);
        }
    }

    /** A resolving effect asks its controller to choose one permanent to transform. */
    record TransformChosenPermanent() implements PermanentChoiceContext {}

    /** Enchantment Alteration: move the targeted Aura to another permanent of the same type. */
    record AttachTargetAuraToAnotherPermanentOfSameType(UUID auraPermanentId) implements PermanentChoiceContext {}

    /** Simic Guildmage: move the targeted Aura to another permanent controlled by its host's controller. */
    record AttachTargetAuraToAnotherPermanentWithSameController(UUID auraPermanentId)
            implements PermanentChoiceContext {}

    record LegendRule(String cardName) implements PermanentChoiceContext {}

    record BounceCreature(UUID bouncingPlayerId, PermanentPredicate thenCondition, CardEffect thenEffect)
            implements PermanentChoiceContext {
        public BounceCreature(UUID bouncingPlayerId) {
            this(bouncingPlayerId, null, null);
        }
    }

    /** A chosen permanent is returned to hand, then a reflexive follow-up resolves. */
    record BouncePermanentThen(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                               CardEffect thenEffect) implements PermanentChoiceContext {}
    /** Return a chosen permanent, then put a +1/+1 counter on the source permanent. */
    record ReturnPermanentAndPutCounterOnSource(UUID controllerId, Card sourceCard,
                                                UUID sourcePermanentId) implements PermanentChoiceContext {}
    record ChoosePlayerThenReturnCreatureToHand(String sourceCardName) implements PermanentChoiceContext {}
    record MayReturnPermanentToHandAndEnterWithCounters(
            Card sourceCard,
            UUID controllerId,
            MayReturnPermanentToHandAndEnterWithCountersEffect effect,
            UUID sourcePermanentId,
            UUID targetCardId
    ) implements PermanentChoiceContext {}

    record SpellRetarget(UUID spellCardId) implements PermanentChoiceContext {}

    record PsychicBattleRetarget(UUID spellCardId, UUID controllerId, Card sourceCard, int targetIndex)
            implements PermanentChoiceContext {}

    record SacrificeCreature(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    record LandEquilibriumSacrifice(UUID sacrificingPlayerId, Card enteringCard,
                                    Zone landPlayZone, int remainingReplacements)
            implements PermanentChoiceContext {}

    record TargetPlayerSacrificesCreatureThenDrawsPower(
            UUID sacrificingPlayerId, UUID drawingPlayerId, Card sourceCard) implements PermanentChoiceContext {}

    /** A targeted player chooses a permanent to sacrifice before taking mana-value damage. */
    record TargetPlayerSacrificesPermanentThenDealsManaValueDamage(
            UUID sacrificingPlayerId, StackEntry resolvingEntry, PermanentPredicate filter)
            implements PermanentChoiceContext {}

    /** Kethek: the controller is choosing another creature to sacrifice before the library reveal. */
    record SacrificeOtherCreatureThenRevealUntilLowerManaValue(
            UUID controllerId, Card sourceCard, com.github.laxika.magicalvibes.model.filter.CardPredicate predicate)
            implements PermanentChoiceContext {}

    /** Eddie Brock: choose another creature to sacrifice before drawing and putting a permanent. */
    record SacrificeAnotherCreatureDrawAndMayPutPermanent(
            UUID controllerId, Card sourceCard, SacrificeAnotherCreatureDrawAndMayPutPermanentEffect effect)
            implements PermanentChoiceContext {}

    /** Torment of Hailfire: {@code playerId} sacrifices the chosen nonland permanent they control. */
    record TormentSacrifice(UUID playerId) implements PermanentChoiceContext {}

    /** The chosen creature is destroyed, or exiled instead when {@code exile} is true (Doomfall). */
    record DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName, boolean exile,
                                 boolean cannotBeRegenerated, UUID sourcePermanentId, Card sourceCard)
            implements PermanentChoiceContext {
        public DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName) {
            this(choosingPlayerId, sourceCardName, false, false, null, null);
        }

        public DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName, boolean exile) {
            this(choosingPlayerId, sourceCardName, exile, false, null, null);
        }

        public DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName, boolean exile,
                                     boolean cannotBeRegenerated) {
            this(choosingPlayerId, sourceCardName, exile, cannotBeRegenerated, null, null);
        }

        public DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName, boolean exile,
                                     UUID sourcePermanentId, Card sourceCard) {
            this(choosingPlayerId, sourceCardName, exile, false, sourcePermanentId, sourceCard);
        }
    }

    /** A player chooses a matching permanent to exile during a resolving effect. */
    record ExileChosenPermanent(UUID choosingPlayerId, String sourceCardName, String permanentLabel)
            implements PermanentChoiceContext {}

    /** The controller chooses the opponent who will choose a matching permanent to sacrifice. */
    record ChooseOpponentForPermanentSacrifice(UUID sacrificingPlayerId, String sourceCardName,
                                                PermanentPredicate filter) implements PermanentChoiceContext {}

    /** The chosen opponent chooses a matching permanent controlled by the sacrificing player. */
    record OpponentChoosesPermanentToSacrifice(UUID choosingPlayerId, UUID sacrificingPlayerId,
                                               String sourceCardName, PermanentPredicate filter)
            implements PermanentChoiceContext {}

    /** The controller chooses the opponent who will choose a permanent to exile until the source leaves. */
    record ChooseOpponentForPermanentExile(Card sourceCard, UUID sourcePermanentId,
                                           UUID controllerId, PermanentPredicate filter)
            implements PermanentChoiceContext {}

    /** The chosen opponent chooses a matching permanent controlled by the source's controller to exile. */
    record OpponentChoosesPermanentToExile(Card sourceCard, UUID sourcePermanentId,
                                           UUID choosingPlayerId, UUID controllerId,
                                           PermanentPredicate filter)
            implements PermanentChoiceContext {}

    /** The controller chooses one matching permanent they control to exile until the source leaves. */
    record PermanentYouControlToExile(Card sourceCard, UUID sourcePermanentId, UUID controllerId,
                                      PermanentPredicate filter) implements PermanentChoiceContext {}

    /** Godsend: choose one creature blocking or blocked by the equipped creature to exile. */
    record ExileCombatOpponent(UUID sourcePermanentId, Card sourceCard) implements PermanentChoiceContext {}

    /** An attack trigger asks the defending player to choose an untapped creature that must block. */
    record DefendingPlayerChoosesCreatureToBlock(UUID choosingPlayerId, UUID sourcePermanentId,
                                                 String sourceCardName) implements PermanentChoiceContext {}

    /** Balduvian Warlord's controller chooses an attacking creature for the removed blocker to block. */
    record BalduvianWarlordChoosesAttacker(UUID blockerId, String sourceCardName)
            implements PermanentChoiceContext {}

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

    /** Rohgahh of Kher Keep: the ability controller chooses which opponent gains the tapped permanents. */
    record ChooseOpponentGainsControlOfSourceAndMatchingPermanents(
            UUID choosingPlayerId,
            String sourceCardName,
            List<UUID> affectedPermanentIds
    ) implements PermanentChoiceContext {
        public ChooseOpponentGainsControlOfSourceAndMatchingPermanents {
            affectedPermanentIds = List.copyOf(affectedPermanentIds);
        }
    }

    /** Murmurs from Beyond: the controller chooses which opponent makes the revealed-card choice. */
    record MurmursFromBeyondOpponentChoice(UUID controllerId, List<Card> revealedCards)
            implements PermanentChoiceContext {
        public MurmursFromBeyondOpponentChoice {
            revealedCards = List.copyOf(revealedCards);
        }
    }

    /** Allure of the Unknown: the controller chooses which opponent makes the revealed-card choice. */
    record AllureOfTheUnknownOpponentChoice(UUID controllerId, List<Card> revealedCards)
            implements PermanentChoiceContext {
        public AllureOfTheUnknownOpponentChoice {
            revealedCards = List.copyOf(revealedCards);
        }
    }

    record MemoriesReturningOpponentChoice(UUID controllerId, List<Card> remainingCards,
                                           int phase, String sourceCardName)
            implements PermanentChoiceContext {
        public MemoriesReturningOpponentChoice {
            remainingCards = List.copyOf(remainingCards);
        }
    }

    /** Guided Passage: the controller chooses which opponent makes the library choice. */
    record GuidedPassageOpponentChoice(UUID controllerId, List<Card> library)
            implements PermanentChoiceContext {
        public GuidedPassageOpponentChoice {
            library = List.copyOf(library);
        }
    }

    /** Mausoleum Turnkey: the controller chooses which opponent chooses the graveyard target. */
    record MausoleumTurnkeyOpponentChoice(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<Card> matchingCards)
            implements PermanentChoiceContext {
        public MausoleumTurnkeyOpponentChoice {
            effects = List.copyOf(effects);
            matchingCards = List.copyOf(matchingCards);
        }
    }

    /**
     * Echo Chamber: {@code choosingPlayerId} picks one creature they control; a token copy of it is
     * then created under {@code copyControllerId}'s control from {@code sourceCard}.
     */
    record OpponentChoosesCreatureTheyControlToCopy(
            UUID choosingPlayerId,
            UUID copyControllerId,
            Card sourceCard
    ) implements PermanentChoiceContext {}

    /** The controller chooses an artifact or creature they control to copy. */
    record ChooseControlledArtifactOrCreatureToCopy(Card sourceCard, UUID controllerId)
            implements PermanentChoiceContext {}

    record ChooseOpponentCreatureThenBoostOthers(
            UUID sourcePermanentId,
            Card sourceCard,
            UUID controllerId,
            int powerBoost,
            int toughnessBoost
    ) implements PermanentChoiceContext {}

    /** Awaken the Maelstrom: choose a permanent you control for the token-copy effect. */
    record AwakenTheMaelstromPermanentCopyChoice(UUID controllerId, Card sourceCard)
            implements PermanentChoiceContext {}

    /** Resolution-time choice of a permanent controlled by the ability's controller to copy. */
    record ChosenPermanentCopyChoice(UUID controllerId, Card sourceCard, PermanentPredicate filter)
            implements PermanentChoiceContext {}

    /** Awaken the Maelstrom: choose a creature for the next counter allocation. */
    record AwakenTheMaelstromCounterCreatureChoice() implements PermanentChoiceContext {}

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

    /** A resolving effect asks its controller which of its chosen targets receives a counter. */
    record PutCounterOnEitherTarget(Card sourceCard, UUID controllerId, CounterType counterType,
                                    List<UUID> targetIds) implements PermanentChoiceContext {
        public PutCounterOnEitherTarget {
            targetIds = List.copyOf(targetIds);
        }
    }
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

    /** Clackbridge Troll: the accepting opponent is picking which creature to sacrifice. */
    record AnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw(
            UUID sacrificingPlayerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect effect)
            implements PermanentChoiceContext {}

    /** Argothian Wurm: the accepting player is picking which land to sacrifice. */
    record AnyPlayerMaySacrificeLandPutSourceOnTop(
            UUID sacrificingPlayerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect effect)
            implements PermanentChoiceContext {}

    /** Brain Gorgers: the accepting player is picking which creature to sacrifice. */
    record AnyPlayerMaySacrificeCreatureToCounterSpell(
            UUID sacrificingPlayerId, Card sourceCard,
            com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreatureToCounterSpellEffect effect)
            implements PermanentChoiceContext {}

    record ForcedCostOrElse(UUID controllerId, UUID sourcePermanentId, Card sourceCard,
                            com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect effect) implements PermanentChoiceContext {}

    /** {@code lifeGainerId} is the sacrificing player for Devour Flesh, the controller otherwise. */
    record SacrificeCreatureControllerGainsLifeEqualToToughness(UUID sacrificingPlayerId, UUID lifeGainerId, String sourceCardName) implements PermanentChoiceContext {}

    /**
     * An activated ability whose permanent-choice cost is being paid one choice at a time.
     * The ability definition and source snapshot are retained because paying an earlier choice
     * may remove the source from the battlefield before the remaining choices are answered.
     */
    record ActivatedAbilityCostChoice(UUID activatingPlayerId,
                                      UUID sourcePermanentId,
                                      Integer abilityIndex,
                                      Integer xValue,
                                      UUID targetId,
                                      Zone targetZone,
                                      List<UUID> targetIds,
                                      CardEffect costEffect,
                                      int remaining,
                                      List<UUID> chosenSoFar,
                                      ActivatedAbility ability,
                                      Permanent sourcePermanentSnapshot,
                                      Card sourceCard) implements PermanentChoiceContext {

        public ActivatedAbilityCostChoice {
            targetIds = targetIds != null ? List.copyOf(targetIds) : List.of();
        }
        /** Permanents already paid toward this cost, for costs whose valid choices depend on prior
         *  picks (e.g. "tap two creatures that share a creature type"). Empty for count-only costs. */
        public ActivatedAbilityCostChoice(UUID activatingPlayerId, UUID sourcePermanentId, Integer abilityIndex,
                                          Integer xValue, UUID targetId, Zone targetZone, CardEffect costEffect,
                                          int remaining) {
            this(activatingPlayerId, sourcePermanentId, abilityIndex, xValue, targetId, targetZone, List.of(),
                    costEffect, remaining, List.of(), null, null, null);
        }

        public ActivatedAbilityCostChoice(UUID activatingPlayerId, UUID sourcePermanentId, Integer abilityIndex,
                                          Integer xValue, UUID targetId, Zone targetZone, CardEffect costEffect,
                                          int remaining, List<UUID> chosenSoFar) {
            this(activatingPlayerId, sourcePermanentId, abilityIndex, xValue, targetId, targetZone, List.of(),
                    costEffect, remaining, chosenSoFar, null, null, null);
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

    /** An activated ability whose sole target is chosen by an opponent selected by its controller. */
    record ActivatedAbilitySoleOpponentTarget(UUID activatingPlayerId,
                                              UUID choosingPlayerId,
                                              UUID sourcePermanentId,
                                              Integer abilityIndex,
                                              Integer xValue,
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
     * @param excludedGraveyardCardId a card excluded by an "other" graveyard target restriction,
     *                                usually the creature that caused the trigger; {@code null}
     *                                when no such restriction applies.
     */
    record DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                              Integer eventValue, Permanent sourcePermanentSnapshot,
                              TargetFilter targetFilter, UUID excludedGraveyardCardId,
                              boolean creaturesOnly) implements PermanentChoiceContext {

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects) {
            this(dyingCard, controllerId, effects, null, null, null, null, true);
        }

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                                  Integer eventValue) {
            this(dyingCard, controllerId, effects, eventValue, null, null, null, true);
        }

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                                  Integer eventValue, Permanent sourcePermanentSnapshot) {
            this(dyingCard, controllerId, effects, eventValue, sourcePermanentSnapshot, null, null, true);
        }

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                                  Integer eventValue, Permanent sourcePermanentSnapshot,
                                  TargetFilter targetFilter) {
            this(dyingCard, controllerId, effects, eventValue, sourcePermanentSnapshot,
                    targetFilter, null, true);
        }

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                                  Integer eventValue, Permanent sourcePermanentSnapshot,
                                  UUID excludedGraveyardCardId) {
            this(dyingCard, controllerId, effects, eventValue, sourcePermanentSnapshot,
                    null, excludedGraveyardCardId, true);
        }

        public DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects,
                                  Integer eventValue, Permanent sourcePermanentSnapshot,
                                  boolean creaturesOnly) {
            this(dyingCard, controllerId, effects, eventValue, sourcePermanentSnapshot,
                    null, null, creaturesOnly);
        }
    }

    /** Targeted ability whose source permanent triggered, with the target chosen as it is put on the stack. */
    record SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      String eventDescription, UUID sourcePermanentId,
                                      Integer eventValue) implements PermanentChoiceContext {
        public SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, "leaves-the-battlefield", null, null);
        }

        public SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          String eventDescription) {
            this(sourceCard, controllerId, effects, eventDescription, null, null);
        }

        public SelfTriggeredAbilityTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          String eventDescription, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, eventDescription, sourcePermanentId, null);
        }
    }

    record DiscardTriggerAnyTarget(Card discardedCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record ResolvingModalTarget(Card sourceCard, UUID controllerId) implements PermanentChoiceContext {}

    record MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, Permanent sourcePermanentSnapshot,
                                   int eventValue, int xValue, boolean optionalTarget) implements PermanentChoiceContext {

        public MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       UUID sourcePermanentId, Permanent sourcePermanentSnapshot,
                                       int eventValue, int xValue) {
            this(sourceCard, controllerId, effects, sourcePermanentId, sourcePermanentSnapshot,
                    eventValue, xValue, false);
        }
        public MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       UUID sourcePermanentId, Permanent sourcePermanentSnapshot) {
            this(sourceCard, controllerId, effects, sourcePermanentId, sourcePermanentSnapshot, 0, 0);
        }

        public MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       UUID sourcePermanentId, Permanent sourcePermanentSnapshot,
                                       int eventValue) {
            this(sourceCard, controllerId, effects, sourcePermanentId, sourcePermanentSnapshot, eventValue, 0);
        }

        public MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, null, null, 0, 0);
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

    /** Guard Dogs: choose a permanent you control to compare with the targeted creature's colors. */
    record GuardDogsPermanentChoice(UUID controllerId) implements PermanentChoiceContext {}

    record RedirectDamageSourceChoice(UUID controllerId, int amount, UUID redirectTargetId) implements PermanentChoiceContext {}

    /** "All damage that would be dealt to target creature this turn by a source of your choice is dealt to
     *  this creature instead." Chooses the source permanent; {@code protectedCreatureId} is the ability's
     *  target and {@code redirectTargetId} is where redirected damage goes (Oracle's Attendants). When
     *  {@code nextEventOnly} is true, only the next single damage event from the chosen source is
     *  redirected before the shield is consumed (Jade Monolith); otherwise all such damage this turn. */
    record RedirectCreatureDamageSourceChoice(UUID controllerId, UUID protectedCreatureId, UUID redirectTargetId,
                                              boolean nextEventOnly) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to you this turn, that damage is dealt
     *  to target creature you control instead." Chooses the source permanent; the target creature is
     *  stored as the redirect destination. */
    record RedirectPlayerDamageSourceChoice(UUID controllerId, UUID redirectTargetId) implements PermanentChoiceContext {}

    record PreventDamageToTargetFromSourceChoice(UUID controllerId, int amount, UUID targetId,
                                                 boolean allDamage) implements PermanentChoiceContext {
        public PreventDamageToTargetFromSourceChoice(UUID controllerId, int amount, UUID targetId) {
            this(controllerId, amount, targetId, false);
        }
    }

    /** "The next time a source of your choice would deal damage to you this turn, prevent that damage."
     *  Any-color source. When {@code gainLife} is true the controller also gains life equal to the
     *  damage prevented (Reverse Damage); when false there is no life gain (Pentagram of the Ages).
     *  When {@code exileFromLibrary} is true the controller instead exiles that many cards from the
     *  top of their library (Bone Mask). */
    record PreventNextDamageFromSourceChoice(UUID controllerId, boolean gainLife,
                                             boolean exileFromLibrary,
                                             Card damageSourceControllerCard,
                                             boolean preventHalfDamage,
                                             boolean drawCards) implements PermanentChoiceContext {
        public PreventNextDamageFromSourceChoice(UUID controllerId, boolean gainLife,
                                                 boolean exileFromLibrary) {
            this(controllerId, gainLife, exileFromLibrary, null, false, false);
        }

        public PreventNextDamageFromSourceChoice(UUID controllerId, boolean gainLife,
                                                 boolean exileFromLibrary, Card damageSourceControllerCard) {
            this(controllerId, gainLife, exileFromLibrary, damageSourceControllerCard, false, false);
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

    /** A targeted attack trigger. {@code choosingPlayerId} defaults to the ability controller and
     *  differs only for text such as Erithizon's "of defending player's choice". */
    record AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                               UUID sourcePermanentId, UUID choosingPlayerId,
                               UUID attackedTargetId, UUID triggeringPermanentId) implements PermanentChoiceContext {

        public AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, UUID choosingPlayerId,
                                   UUID attackedTargetId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, choosingPlayerId,
                    attackedTargetId, null);
        }

        public AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, controllerId, null, null);
        }

        public AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, UUID choosingPlayerId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, choosingPlayerId, null, null);
        }
    }

    /** Remembers the attack target for each token entering tapped and attacking. */
    record CreateTokensAttacking(UUID controllerId, Card sourceCard,
                                 com.github.laxika.magicalvibes.model.effect.CreateTokenEffect tokenEffect,
                                 int amount, int tokenCount, boolean sacrificeAtEndStep,
                                 List<UUID> chosenAttackTargets) implements PermanentChoiceContext {}

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
                                 UUID sourcePermanentId, boolean modesResetEachTurn,
                                 boolean consumeModes, UUID triggeringCardId) implements PermanentChoiceContext {

        public TriggeredModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                     UUID sourcePermanentId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, false, false, null);
        }

        public TriggeredModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                     UUID sourcePermanentId, boolean modesResetEachTurn) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn, false, null);
        }

        public TriggeredModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                     UUID sourcePermanentId, UUID triggeringCardId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, false, false, triggeringCardId);
        }

        public TriggeredModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                     UUID sourcePermanentId, boolean modesResetEachTurn,
                                     boolean consumeModes) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn,
                    consumeModes, null);
        }

        public TriggeredModalTrigger(Card sourceCard, UUID controllerId, ChooseOneEffect effect,
                                     UUID sourcePermanentId, boolean modesResetEachTurn,
                                     UUID triggeringCardId) {
            this(sourceCard, controllerId, effect, sourcePermanentId, modesResetEachTurn,
                    false, triggeringCardId);
        }
    }

    /** A resolving effect asks its controller to choose an opponent before returning its source under that opponent's control. */
    record ChooseOpponentForSelfFlicker(UUID sourcePermanentId, UUID controllerId, String sourceCardName)
            implements PermanentChoiceContext {}

    /** Kaya, Spirits' Justice: the controller chooses a token to copy the selected creature card. */
    record KayaSpiritsJusticeTokenChoice(Card sourceCard, UUID controllerId, Card chosenCard)
            implements PermanentChoiceContext {}

    /** Targeted "whenever you cycle or discard a card" trigger on a battlefield permanent
     *  ({@code EffectSlot.ON_CONTROLLER_DISCARDS}), e.g. Zenith Seeker — "target creature gains
     *  flying until end of turn." The controller chooses the target when the discard trigger is
     *  serviced; mirrors {@link EntersTriggerTarget}'s any-permanent target flow. */
    record DiscardControllerTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID sourcePermanentId, int discardedCount)
            implements PermanentChoiceContext {

        public DiscardControllerTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                               UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, 0);
        }
    }

    record SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                       boolean playerTargetOnly, TargetFilter targetFilter,
                                       int spellManaSpentX, UUID sourcePermanentId,
                                       Permanent sourcePermanentSnapshot, boolean optionalTarget,
                                       UUID triggeringPermanentId, UUID permanentTargetControllerId,
                                       UUID choosingPlayerId)
            implements PermanentChoiceContext {

        /** Convenience constructor for any-target (permanents + players). */
        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, false, null, 0, null, null, false, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly) {
            this(sourceCard, controllerId, effects, playerTargetOnly, null, 0, null, null, false, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter, 0, null, null, false, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, null, null, false, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, false, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           Permanent sourcePermanentSnapshot) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, sourcePermanentSnapshot, false, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           boolean optionalTarget) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, optionalTarget, null, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           UUID triggeringPermanentId) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, false, triggeringPermanentId, null, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           boolean playerTargetOnly, TargetFilter targetFilter,
                                           int spellManaSpentX, UUID sourcePermanentId,
                                           boolean optionalTarget, UUID permanentTargetControllerId) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter,
                    spellManaSpentX, sourcePermanentId, null, optionalTarget, null,
                    permanentTargetControllerId, controllerId);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID sourcePermanentId, UUID choosingPlayerId) {
            this(sourceCard, controllerId, effects, false, null, 0, sourcePermanentId, null,
                    false, null, null, choosingPlayerId);
        }
    }

    record BounceOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

    record BouncePermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

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

    /** Deepfathom Echo: choose another creature the source controller controls to copy until end of turn. */
    record DeepfathomEchoCreatureChoice(UUID controllerId, UUID sourcePermanentId)
            implements PermanentChoiceContext {}

    /** Choose the creature whose copiable characteristics will be used. */
    record PolymorphousRushCreatureChoice(UUID controllerId,
                                           MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect effect)
            implements PermanentChoiceContext {}

    record CopySpellForOtherControlledCreatureChoice(CopySpellForEachOtherControlledCreatureEffect effect)
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
                                     TargetFilter targetFilter, UUID choosingPlayerId,
                                     boolean anyNumberTargets, UUID excludedPlayerId) implements PermanentChoiceContext {

        public UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null, controllerId, false, null);
        }

        public UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, sourcePermanentId, targetFilter, controllerId, false, null);
        }

        public UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, TargetFilter targetFilter, UUID choosingPlayerId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, targetFilter, choosingPlayerId, false, null);
        }

        public UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                         UUID sourcePermanentId, TargetFilter targetFilter, UUID choosingPlayerId,
                                         boolean anyNumberTargets, UUID excludedPlayerId) {
            this.sourceCard = sourceCard;
            this.controllerId = controllerId;
            this.effects = List.copyOf(effects);
            this.sourcePermanentId = sourcePermanentId;
            this.targetFilter = targetFilter;
            this.choosingPlayerId = choosingPlayerId;
            this.anyNumberTargets = anyNumberTargets;
            this.excludedPlayerId = excludedPlayerId;
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
                                        UUID sourcePermanentId, TargetFilter targetFilter,
                                        UUID choosingPlayerId) implements PermanentChoiceContext {

        public UpkeepPermanentTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                            UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null, null);
        }

        public UpkeepPermanentTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                            UUID sourcePermanentId, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, sourcePermanentId, targetFilter, null);
        }
    }

    /** "Whenever this permanent phases in, target …" — queued from {@code ON_SELF_PHASES_IN} during
     *  the untap-step phasing action; drained at the start of upkeep when the trigger is put on the
     *  stack. Mirrors {@link UpkeepPermanentTargetTrigger}'s permanent-target flow. */
    record PhasesInTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepSecondPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId, UUID firstTargetPlayerId) implements PermanentChoiceContext {}

    record UpkeepCopyTriggerTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                                   BecomeCopyOfTargetCreatureEffect effect) implements PermanentChoiceContext {
        public UpkeepCopyTriggerTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
            this(sourceCard, controllerId, sourcePermanentId, new BecomeCopyOfTargetCreatureEffect());
        }
    }

    record CapriciousEfreetOwnTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Puca's Mischief step 1: choose the nonland permanent you control. {@code effects} carries the
     *  wrapping {@link com.github.laxika.magicalvibes.model.effect.MayEffect} so it reaches the stack. */
    record PucasMischiefOwnTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Puca's Mischief step 2: choose the opponent's nonland permanent (mana value &le; {@code ownTargetId}'s). */
    record PucasMischiefOpponentTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId, UUID ownTargetId) implements PermanentChoiceContext {}

    record EndStepTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record BeginningOfCombatTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record DayNightTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record DayNightTransformAttachment(UUID permanentId, UUID controllerId) implements PermanentChoiceContext {}

    record LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                  StackEntryType spellType, List<Card> cardsToBottom,
                                  Integer discoverValue) implements PermanentChoiceContext {
        public LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                      StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, null, null);
        }

        public LibraryCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                      StackEntryType spellType, List<Card> cardsToBottom) {
            this(cardToCast, controllerId, spellEffects, spellType, cardsToBottom, null);
        }

        public LibraryCastSpellTarget {
            if (cardsToBottom != null) {
                cardsToBottom = List.copyOf(cardsToBottom);
            }
        }
    }

    record SacrificeArtifactForDividedDamage(UUID controllerId, Card sourceCard, Map<UUID, Integer> damageAssignments) implements PermanentChoiceContext {}

    /** Heart-Piercer Manticore: choose the creature whose sacrifice creates the reflexive trigger. */
    record SacrificeAnotherCreatureDealPowerDamage(UUID controllerId, Card sourceCard) implements PermanentChoiceContext {}

    record SacrificeAnotherCreatureGainLifeAndDraw(UUID controllerId, Card sourceCard) implements PermanentChoiceContext {}

    record SacrificeCreatureThenMassDamageEqualToPower(UUID controllerId, Card sourceCard) implements PermanentChoiceContext {}

    record ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType,
                                boolean copy, List<UUID> chosenTargets, int genericCostReduction,
                                boolean resolutionCast, int lifeLossAfterCast,
                                boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard) implements PermanentChoiceContext {
        // {@code copy=true} marks a Paradigm copy that must cease to exist rather than being placed in
        // a zone (CR 707.10a) — both on resolution and when it can't be legally cast. Defaults to false
        // for real cards cast from exile.
        // {@code chosenTargets} accumulates already-selected targets, in the card's declared target
        // order, while a multi-target spell walks its target slots one at a time. Empty for the
        // single-target path (which stores its lone target as the StackEntry's {@code targetId}).
        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType, boolean copy) {
            this(cardToCast, controllerId, spellEffects, spellType, copy, List.of(), 0, false, 0, false);
        }

        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, false, List.of(), 0, false, 0, false);
        }

        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                    StackEntryType spellType, boolean copy, List<UUID> chosenTargets) {
            this(cardToCast, controllerId, spellEffects, spellType, copy, chosenTargets, 0, false, 0, false);
        }

        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                    StackEntryType spellType, boolean copy, List<UUID> chosenTargets,
                                    int genericCostReduction) {
            this(cardToCast, controllerId, spellEffects, spellType, copy, chosenTargets,
                    genericCostReduction, false, 0, false);
        }

        public ExileCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                    StackEntryType spellType, boolean copy, List<UUID> chosenTargets,
                                    boolean resolutionCast, int lifeLossAfterCast) {
            this(cardToCast, controllerId, spellEffects, spellType, copy, chosenTargets,
                    0, resolutionCast, lifeLossAfterCast, false);
        }

        public static ExileCastSpellTarget resolutionCastCopy(Card cardToCast, UUID controllerId,
                                                               List<CardEffect> spellEffects,
                                                               StackEntryType spellType,
                                                               int lifeLossAfterCast) {
            return new ExileCastSpellTarget(cardToCast, controllerId, spellEffects, spellType,
                    true, List.of(), 0, true, lifeLossAfterCast, false);
        }
    }

    record ChandraTorchCastSpellTarget(Card cardToCast, UUID controllerId, Card sourceCard, int damage,
                                       List<UUID> chosenTargets) implements PermanentChoiceContext {}

    record VaanCastSpellTarget(Card cardToCast, UUID controllerId, Card sourceCard,
                               UUID sourcePermanentId, List<UUID> chosenTargets) implements PermanentChoiceContext {}

    record GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                    StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                    boolean withoutPayingManaCost, UUID ownerId,
                                    boolean restrictAdditionalSpellsThisTurn,
                                    boolean anyManaType,
                                    int copyCount) implements PermanentChoiceContext {

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                        boolean withoutPayingManaCost) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard,
                    withoutPayingManaCost, null, false, false, 0);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                        boolean withoutPayingManaCost, UUID ownerId) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard,
                    withoutPayingManaCost, ownerId, false, false, 0);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                        boolean withoutPayingManaCost, UUID ownerId, int copyCount) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard,
                    withoutPayingManaCost, ownerId, false, false, copyCount);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                        boolean withoutPayingManaCost, UUID ownerId,
                                        boolean restrictAdditionalSpellsThisTurn) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard,
                    withoutPayingManaCost, ownerId, restrictAdditionalSpellsThisTurn, false, 0);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                        StackEntryType spellType, boolean exileInsteadOfGraveyard,
                                        boolean withoutPayingManaCost, UUID ownerId,
                                        boolean restrictAdditionalSpellsThisTurn, boolean anyManaType) {
            this(cardToCast, controllerId, spellEffects, spellType, exileInsteadOfGraveyard,
                    withoutPayingManaCost, ownerId, restrictAdditionalSpellsThisTurn, anyManaType, 0);
        }

        public GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, false, true, null, false, false, 0);
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
                               StackEntryType spellType, int xValue, boolean castForMadnessCost)
            implements PermanentChoiceContext {

        public HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) {
            this(cardToCast, controllerId, spellEffects, spellType, 0, false);
        }

        public HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects,
                                   StackEntryType spellType, int xValue) {
            this(cardToCast, controllerId, spellEffects, spellType, xValue, false);
        }
    }

    /** A spell whose controller chooses an opponent, then that opponent chooses the creature target. */
    record OpponentChosenSpellTarget(Player caster, Card cardToCast, int cardIndex, Integer xValue,
                                     boolean buyback, UUID chosenOpponentId)
            implements PermanentChoiceContext {}

    record ChooseCreatureAsEnter(UUID enteringPermanentId, UUID controllerId, Card card, UUID targetId,
                                 boolean wasCastFromHand, int etbMode, boolean kicked) implements PermanentChoiceContext {}

    record ChooseEquipmentToAttachAsEnter(UUID equipmentPermanentId, UUID controllerId, Card card,
                                          UUID targetId, boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds,
                                          List<String> repeatedAdditionalCosts,
                                          List<UUID> convokeCreatureIds) implements PermanentChoiceContext {
        public ChooseEquipmentToAttachAsEnter {
            targetIds = List.copyOf(targetIds);
            repeatedAdditionalCosts = List.copyOf(repeatedAdditionalCosts);
            convokeCreatureIds = List.copyOf(convokeCreatureIds);
        }
    }

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

    /** A controller-draw trigger whose effect targets a permanent through the card's target filter. */
    record DrawTriggerPermanentTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, TargetFilter targetFilter) implements PermanentChoiceContext {}

    /** An enters-the-battlefield trigger that needs an "any target" choice and whose effect resolves
     *  against the permanent that just entered rather than the triggering permanent — e.g. "that creature
     *  deals damage equal to its power to any target". The {@code sourcePermanentId} points at the
     *  permanent that entered (the damage source); {@code sourceCard} is the permanent whose ability
     *  triggered (Flayer of the Hatebound, Warstorm Surge). */
    record EnteringPermanentAnyTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                              UUID sourcePermanentId, UUID chosenPermanentId,
                                              Integer chosenPermanentPowerAtTrigger)
            implements PermanentChoiceContext {
        public EnteringPermanentAnyTargetTrigger(Card sourceCard, UUID controllerId,
                                                  List<CardEffect> effects, UUID sourcePermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, null, null);
        }

        public EnteringPermanentAnyTargetTrigger(Card sourceCard, UUID controllerId,
                                                  List<CardEffect> effects, UUID sourcePermanentId,
                                                  UUID chosenPermanentId) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenPermanentId, null);
        }
    }

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
                                 UUID sourcePermanentId, TargetFilter targetFilter,
                                 UUID triggeringPermanentId) implements PermanentChoiceContext {

        public ETBTokenTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                     UUID sourcePermanentId, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, sourcePermanentId, targetFilter, null);
        }
    }

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
                                      boolean resumePendingMayResolution,
                                      UUID triggeringCardId) implements PermanentChoiceContext {

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, List.of(), 0, List.of(), false, null);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, 0, List.of(), false, null);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue, List.of(), false, null);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue,
                                          boolean resumePendingMayResolution) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue, List.of(),
                    resumePendingMayResolution, null);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue,
                                          List<String> repeatedAdditionalCosts) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue,
                    repeatedAdditionalCosts, false, null);
        }

        public ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                          UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                          int currentGroupIndex, int chosenInCurrentGroup,
                                          List<Integer> groupSizes, int xValue,
                                          List<String> repeatedAdditionalCosts,
                                          boolean resumePendingMayResolution) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chosenTargetsSoFar,
                    currentGroupIndex, chosenInCurrentGroup, groupSizes, xValue,
                    repeatedAdditionalCosts, resumePendingMayResolution, null);
        }
    }

    /** Saga chapter ability that targets a permanent (e.g. Phyrexian Scriptures chapter I).
     *  {@code targetFilters} restricts valid targets (e.g. "creature an opponent controls"); null/empty = any creature. */
    record SagaChapterTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                             UUID sourcePermanentId, String chapterName,
                             Set<TargetFilter> targetFilters,
                             List<SagaChapterTargetGroup> targetGroups,
                             List<UUID> chosenTargetsSoFar,
                             int currentGroupIndex) implements PermanentChoiceContext {
        public SagaChapterTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 UUID sourcePermanentId, String chapterName,
                                 Set<TargetFilter> targetFilters) {
            this(sourceCard, controllerId, effects, sourcePermanentId, chapterName, targetFilters,
                    List.of(), List.of(), 0);
        }
    }

    record SagaChapterPlayerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                   UUID sourcePermanentId, String chapterName, Set<TargetFilter> targetFilters)
            implements PermanentChoiceContext {}

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

    record HandAbilityCostChoice(UUID activatingPlayerId,
                                 Card handCard,
                                 ActivatedAbility ability,
                                 Integer abilityIndex,
                                 Integer xValue,
                                 UUID targetId,
                                 Zone targetZone,
                                 CardEffect costEffect,
                                 int remaining,
                                 List<UUID> chosenSoFar,
                                 int stackSizeBeforeCosts) implements PermanentChoiceContext {

        public HandAbilityCostChoice(UUID activatingPlayerId, Card handCard, ActivatedAbility ability,
                                     Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone,
                                     CardEffect costEffect, int remaining, int stackSizeBeforeCosts) {
            this(activatingPlayerId, handCard, ability, abilityIndex, xValue, targetId, targetZone,
                    costEffect, remaining, List.of(), stackSizeBeforeCosts);
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
                                       UUID graveyardOwnerId, int minCount, int xValue, int maxCount,
                                       Integer sourcePowerAtTrigger)
            implements PermanentChoiceContext {

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID graveyardOwnerId, int minCount, int xValue, int maxCount) {
            this(sourceCard, controllerId, effects, graveyardOwnerId, minCount, xValue, maxCount, null);
        }

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, null, 0, 0, 0, null);
        }

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID graveyardOwnerId) {
            this(sourceCard, controllerId, effects, graveyardOwnerId, 0, 0, 0, null);
        }

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID graveyardOwnerId, int minCount) {
            this(sourceCard, controllerId, effects, graveyardOwnerId, minCount, 0, 0, null);
        }

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           UUID graveyardOwnerId, int minCount, int xValue) {
            this(sourceCard, controllerId, effects, graveyardOwnerId, minCount, xValue, 0, null);
        }

        public SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                           int xValue) {
            this(sourceCard, controllerId, effects, null, 0, xValue, 0, null);
        }
    }

    /** "Sacrifice a [permanent]. If you do, [effect]." (e.g. The First Eruption chapter III). */
    record SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect,
                                  UUID sourcePermanentId, boolean reflexive) implements PermanentChoiceContext {
        public SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect,
                                      boolean reflexive) {
            this(controllerId, sourceCard, thenEffect, null, reflexive);
        }

        public SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect) {
            this(controllerId, sourceCard, thenEffect, null, true);
        }
    }

    /** Victimize: sacrifice a creature, then return the selected graveyard cards if the sacrifice happened. */
    record SacrificePermanentAndReturnTargetCards(UUID controllerId, Card sourceCard,
                                                  SacrificePermanentAndReturnTargetCardsFromGraveyardEffect effect)
            implements PermanentChoiceContext {}

    /** "Sacrifice another permanent. If you do, this creature gets +X/+Y." */
    record SacrificePermanentAndBoostSelf(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                          int power, int toughness, String permanentDescription,
                                          Set<Keyword> grantedKeywords) implements PermanentChoiceContext {
        public SacrificePermanentAndBoostSelf(UUID controllerId, Card sourceCard, UUID sourcePermanentId,
                                              int power, int toughness, String permanentDescription) {
            this(controllerId, sourceCard, sourcePermanentId, power, toughness,
                    permanentDescription, Set.of());
        }
    }

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

    /** Each targeted player chooses a creature to sacrifice after the life loss has been applied. */
    record EachTargetPlayerLosesLifeAndSacrificesCreature(
            UUID choosingPlayerId,
            UUID sourceControllerId,
            Card sourceCard,
            UUID sourcePermanentId,
            List<UUID> remainingTargetPlayerIds,
            List<UUID> chosenCreatureIds
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

    /** A mana ability where the activating player chooses the recipient. */
    record ManaAbilityAddToChosenPlayer(ManaColor color, int amount, boolean creatureSource,
                                        String sourceCardName, boolean anyColor, UUID controllerId)
            implements PermanentChoiceContext {

        public ManaAbilityAddToChosenPlayer(ManaColor color, int amount, boolean creatureSource,
                                            String sourceCardName) {
            this(color, amount, creatureSource, sourceCardName, false, null);
        }
    }

    /** Bend or Break: a player chooses which opponent will choose one of their land piles. */
    record BendOrBreakOpponentChoice(UUID playerId) implements PermanentChoiceContext {}

    record OpponentChoosesCardFromGraveyardToHand() implements PermanentChoiceContext {}

    /** Curator of Destinies: the controller chooses which opponent chooses between the two piles. */
    record CuratorOpponentChoice() implements PermanentChoiceContext {}

    /** Tariff tie-break: {@code playerId} chooses which of their creatures tied for greatest mana
     *  value is the one they must pay for or sacrifice. */
    record TariffTieBreak(UUID playerId, Card sourceCard) implements PermanentChoiceContext {}

    /** Dispersal tie-break: the opponent chooses which tied nonland permanent to return. */
    record DispersalTieBreak(UUID playerId, Card sourceCard) implements PermanentChoiceContext {}

    /** Juxtapose tie-break: a player chooses which of their permanents tied for greatest mana value
     *  participates in the exchange. {@code artifactPhase} distinguishes the creature step from the
     *  artifact step. While {@code controllerChosen} is false the pending choice belongs to the spell's
     *  controller; once true, {@code controllerPermanentId} holds the controller's already-selected
     *  permanent and the pending choice belongs to the target player. */
    record JuxtaposeTieBreak(Card sourceCard, UUID controllerId, UUID targetPlayerId,
                             boolean artifactPhase, boolean controllerChosen,
                             UUID controllerPermanentId) implements PermanentChoiceContext {}

    record ChooseOwnCreatureGrantKeyword(Keyword keyword) implements PermanentChoiceContext {}

    record SuspectChosenOtherCreature() implements PermanentChoiceContext {}

    record TurnOwnCreatureFaceUp() implements PermanentChoiceContext {}

    record PlotTriggerAnyTarget(Card plottedCard, UUID controllerId, List<CardEffect> effects)
            implements PermanentChoiceContext {}

    /** Target choices for the reflexive fight created by Earth Rumble's Earthbend action. */
    record EarthbendThenFightTarget(Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                                    UUID firstTargetId, boolean choosingOpponentTarget)
            implements PermanentChoiceContext {}

}
