package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;



public sealed interface PermanentChoiceContext extends PendingInteraction {

    record CloneCopy() implements PermanentChoiceContext {}

    record AuraGraft(UUID auraPermanentId) implements PermanentChoiceContext {}

    /** Glamer Spinners: move every Aura in {@code auraPermanentIds} onto the chosen permanent. */
    record AttachAllAurasToAnotherPermanent(List<UUID> auraPermanentIds) implements PermanentChoiceContext {}

    /** Stonehewer Giant: attach the just-placed Equipment {@code equipmentPermanentId} to the chosen creature. */
    record AttachEquipmentToCreature(UUID equipmentPermanentId, UUID controllerId) implements PermanentChoiceContext {}

    /** Nettlevine Blight: sacrifice {@code permanentToSacrificeId}, then reattach the source Aura
     *  {@code auraPermanentId} onto the chosen creature or land. */
    record ReattachSourceAuraAfterSacrifice(UUID auraPermanentId, UUID permanentToSacrificeId) implements PermanentChoiceContext {}

    record LegendRule(String cardName) implements PermanentChoiceContext {}

    record BounceCreature(UUID bouncingPlayerId) implements PermanentChoiceContext {}

    record SpellRetarget(UUID spellCardId) implements PermanentChoiceContext {}

    record SacrificeCreature(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    /** Torment of Hailfire: {@code playerId} sacrifices the chosen nonland permanent they control. */
    record TormentSacrifice(UUID playerId) implements PermanentChoiceContext {}

    /** The chosen creature is destroyed, or exiled instead when {@code exile} is true (Doomfall). */
    record DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName, boolean exile) implements PermanentChoiceContext {
        public DestroyChosenCreature(UUID choosingPlayerId, String sourceCardName) {
            this(choosingPlayerId, sourceCardName, false);
        }
    }

    record SacrificeCreatureThenSearchLibrary(UUID sacrificingPlayerId) implements PermanentChoiceContext {}

    record SacrificeCreatureOpponentsLoseLife(UUID sacrificingPlayerId, String sourceCardName) implements PermanentChoiceContext {}

    record ForcedCostOrElse(UUID controllerId, UUID sourcePermanentId, Card sourceCard,
                            com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect effect) implements PermanentChoiceContext {}

    record SacrificeCreatureControllerGainsLifeEqualToToughness(UUID sacrificingPlayerId, UUID controllerId, String sourceCardName) implements PermanentChoiceContext {}

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

    record DeathTriggerTarget(Card dyingCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    /** Targeted "when this permanent leaves the battlefield" trigger ({@code EffectSlot.ON_SELF_LEAVES_BATTLEFIELD}),
     *  e.g. Meadowboon — "put a +1/+1 counter on each creature target player controls." */
    record SelfLeavesTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record DiscardTriggerAnyTarget(Card discardedCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    record MayAbilityTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    /** "Prevent all damage [the chosen source] would deal this turn" — to the controller only
     *  (Auriok Replica) or to everything (Burrenton Forge-Tender). The legal source choices are
     *  already filtered when the choice begins. */
    record PreventDamageSourceChoice(UUID controllerId, boolean controllerOnly) implements PermanentChoiceContext {}

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
     *  damage prevented (Reverse Damage); when false there is no life gain (Pentagram of the Ages). */
    record PreventNextDamageFromSourceChoice(UUID controllerId, boolean gainLife) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to any target this turn, prevent that
     *  damage." (Sanctum Guardian). Protects any recipient, not just the controller. */
    record PreventNextDamageFromSourceToAnyTargetChoice(UUID controllerId) implements PermanentChoiceContext {}

    /** "The next time a source of your choice would deal damage to you this turn, instead that source
     *  deals that much damage to you and Eye for an Eye deals that much damage to that source's
     *  controller." (Eye for an Eye). */
    record EyeForAnEyeSourceChoice(UUID controllerId, Card eyeCard) implements PermanentChoiceContext {}

    record AttackTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

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
     *  enter trigger is serviced; mirrors {@link AttackTriggerTarget}'s any-permanent target flow. */
    record EntersTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** Targeted "whenever you cycle or discard a card" trigger on a battlefield permanent
     *  ({@code EffectSlot.ON_CONTROLLER_DISCARDS}), e.g. Zenith Seeker — "target creature gains
     *  flying until end of turn." The controller chooses the target when the discard trigger is
     *  serviced; mirrors {@link EntersTriggerTarget}'s any-permanent target flow. */
    record DiscardControllerTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly, TargetFilter targetFilter, int spellManaSpentX, UUID sourcePermanentId) implements PermanentChoiceContext {

        /** Convenience constructor for any-target (permanents + players). */
        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects) {
            this(sourceCard, controllerId, effects, false, null, 0, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly) {
            this(sourceCard, controllerId, effects, playerTargetOnly, null, 0, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly, TargetFilter targetFilter) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter, 0, null);
        }

        public SpellTargetTriggerAnyTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, boolean playerTargetOnly, TargetFilter targetFilter, int spellManaSpentX) {
            this(sourceCard, controllerId, effects, playerTargetOnly, targetFilter, spellManaSpentX, null);
        }
    }

    record BounceOwnPermanentOrSacrificeSelf(UUID controllerId, UUID sourceCardId) implements PermanentChoiceContext {}

    /** Champion a creature: exile the chosen creature until the source permanent leaves the battlefield. */
    record ChampionCreature(UUID sourcePermanentId, UUID controllerId) implements PermanentChoiceContext {}

    /** "Put a creature you control on top of its owner's library." The controller chooses one of their
     *  creatures (the source itself is a legal choice) as the effect resolves. (Nulltread Gargantuan.) */
    record PutControlledCreatureOnTopOfLibrary(UUID controllerId) implements PermanentChoiceContext {}
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

    record UpkeepPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepMultiPlayerTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepAnyTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    record UpkeepPermanentTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

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

    /**
     * Heart-Piercer Manticore: its enter trigger's any-target was chosen ({@code targetId}); the
     * controller now picks another creature to sacrifice, whereupon {@code sourceCard} deals that
     * creature's power as damage to {@code targetId}.
     */
    record SacrificeAnotherCreatureDealPowerDamage(UUID controllerId, Card sourceCard, UUID targetId) implements PermanentChoiceContext {}

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

    record GraveyardCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

    record HandCastSpellTarget(Card cardToCast, UUID controllerId, List<CardEffect> spellEffects, StackEntryType spellType) implements PermanentChoiceContext {}

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

    /** "Whenever a creature enters from your graveyard, that creature deals damage equal to its power to
     *  any target." The {@code sourcePermanentId} points at the creature that entered (the damage source);
     *  {@code sourceCard} is the permanent whose ability triggered (e.g. Flayer of the Hatebound). */
    record EntersFromGraveyardTriggerTarget(Card sourceCard, UUID controllerId, List<CardEffect> effects, UUID sourcePermanentId) implements PermanentChoiceContext {}

    /** ETB trigger that needs to target a spell on the stack (e.g. Naru Meha's copy ability). */
    record ETBSpellTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                 StackEntryPredicate spellFilter) implements PermanentChoiceContext {}

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
    record ETBTokenMultiTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects,
                                      UUID sourcePermanentId, List<UUID> chosenTargetsSoFar,
                                      int currentGroupIndex, int chosenInCurrentGroup) implements PermanentChoiceContext {}

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
                                      int remaining) implements PermanentChoiceContext {}

    /** Tap-cost payment for a resolution-time may ability (e.g. Aziza, Mage Tower Captain). */
    record MayAbilityTapCostChoice(UUID playerId,
                                   UUID sourcePermanentId,
                                   com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost costEffect,
                                   int remaining) implements PermanentChoiceContext {}

    /** Spell-cast trigger that needs to target a card in a graveyard (e.g. Teshar, Ancestor's Apostle). */
    record SpellGraveyardTargetTrigger(Card sourceCard, UUID controllerId, List<CardEffect> effects) implements PermanentChoiceContext {}

    /** "Sacrifice a [permanent]. If you do, [effect]." (e.g. The First Eruption chapter III). */
    record SacrificePermanentThen(UUID controllerId, Card sourceCard, CardEffect thenEffect) implements PermanentChoiceContext {}

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

    /** Valleymaker's mana ability ("Choose a player. That player adds {G}{G}{G}."). The activating
     *  player picks the recipient; {@code amount} mana of {@code color} is added to that player's pool
     *  (tracking creature mana when {@code creatureSource}). Begun inline during mana-ability resolution. */
    record ManaAbilityAddToChosenPlayer(ManaColor color, int amount, boolean creatureSource,
                                        String sourceCardName) implements PermanentChoiceContext {}

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
