package com.github.laxika.magicalvibes.model;

public enum EffectSlot {
    ON_TAP,
    /** "Whenever you tap this permanent for mana, …" (Zhur-Taa Druid). Dispatched only from the
     *  mana-ability tap path in {@code AbilityActivationService}, so unlike
     *  {@link #ON_ALLY_PERMANENT_BECOMES_TAPPED} it does not fire when the permanent is tapped to
     *  attack or by an opponent's effect. Being a mana-ability trigger it is deferred into
     *  {@code GameData.pendingManaAbilityTriggers} until a player would next receive priority
     *  (CR 603.3). */
    ON_SELF_TAPPED_FOR_MANA,
    /** "Whenever a mana ability of this permanent resolves." */
    ON_SELF_MANA_ABILITY_RESOLVES,
    ON_ENTER_BATTLEFIELD,
    SPELL,
ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
    ON_ALLY_CREATURES_ENTERS_BATTLEFIELD,
    /** "Whenever a creature or enchantment enters under your control" for a turn-scoped global trigger. */
    ON_ALLY_CREATURE_OR_ENCHANTMENT_ENTERS_BATTLEFIELD,
    /** "Whenever this creature or another creature you control enters, …" — like
     *  {@link #ON_ALLY_CREATURE_ENTERS_BATTLEFIELD} but the source's own entry also triggers it.
     *  Scanned in the same pass ({@code TriggerCollectionService.checkAllyCreatureEntersTriggers})
     *  without the self-exclusion. The entering permanent rides along as the trigger's
     *  {@code triggeringPermanentId} so an effect like
     *  {@code EnteringCreatureFightsTargetCreatureEffect} knows which creature "that creature" is.
     *  Used by Gruul Ragebeast. */
    ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
    /** "Whenever a nontoken creature enters under your control" (excludes this permanent and tokens).
     *  Like {@link #ON_ALLY_CREATURE_ENTERS_BATTLEFIELD} but the entering permanent's id is preserved on
     *  any queued may-pay ability (mirrors {@link #ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD}), so a
     *  "you may pay {N}. If you do, create a token that's a copy of that creature" effect
     *  ({@code CreateTokenCopyOfTargetPermanentEffect}) knows which creature to copy. Checked in
     *  {@code TriggerCollectionService.checkAllyNontokenCreatureEntersTriggers}. Used by Minion Reflector. */
    ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
    ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
    /** "Whenever one or more tokens you control enter the battlefield." */
    ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
    ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
    ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
    /** Aura slot for player-enchanting Curses: "Whenever a creature enchanted player controls enters, …".
     *  Fires once per matching Curse attached to the entering creature's controller. The enchanted player
     *  is baked as the (non-targeting) {@code targetId} so a {@code LoseLifeEffect(TARGET_PLAYER)} lands on
     *  them while an accompanying {@code GainLifeEffect} feeds the Aura's controller ("you"). Checked in
     *  {@code TriggerCollectionService.checkEnchantedPlayerCreatureEntersTriggers}. Used by Trespasser's Curse. */
    ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD,
    STATIC,
    ON_SACRIFICE,
    ON_BLOCK,
    UPKEEP_TRIGGERED,
    GRAVEYARD_UPKEEP_TRIGGERED,
    EACH_UPKEEP_TRIGGERED,
    SUSPENDED_EACH_UPKEEP_TRIGGERED,
    /** Triggers whenever a time counter is removed from this card while it is suspended in exile. */
    ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
    OPPONENT_UPKEEP_TRIGGERED,
    ON_ANY_PLAYER_CASTS_SPELL,
    ON_CONTROLLER_CASTS_SPELL,
    ON_CONTROLLER_GIVES_GIFT,
    /** Triggers whenever a Case is solved by the controller. */
    ON_ALLY_CASE_SOLVES,
    ON_CONTROLLER_FORETELLS,
    /** Triggers when the controller copies an instant or sorcery spell. */
    ON_CONTROLLER_COPIES_SPELL,
    /** Triggers when an opponent copies an instant or sorcery spell. */
    ON_OPPONENT_COPIES_SPELL,
    ON_CONTROLLER_COUNTERS_SPELL,
    /** "Whenever a spell you've cast is countered, ..." */
    ON_CONTROLLER_SPELL_COUNTERED,
    /**
     * "Whenever you play a land" — fired at the actual land-play sites (from hand, from graveyard,
     * from exile, and the may-cast/free-play paths), NOT when a land merely enters the battlefield.
     * Use {@link #ON_ALLY_LAND_ENTERS_BATTLEFIELD} for landfall, which also sees lands put onto the
     * battlefield by an effect.
     */
    ON_CONTROLLER_PLAYS_LAND,
    /**
     * "Whenever an opponent plays a land" — the opponent-side mirror of
     * {@link #ON_CONTROLLER_PLAYS_LAND}: fired at the actual land-play sites only, NOT when a land
     * merely enters the battlefield. Use {@link #ON_OPPONENT_LAND_ENTERS_BATTLEFIELD} for the
     * enters-based version. Dirtcowl Wurm.
     */
    ON_OPPONENT_PLAYS_LAND,
    ON_OPPONENT_CASTS_SPELL,
    ON_DEATH,
    ON_HAUNTED_CREATURE_DIES,
    ON_ALLY_CREATURE_DIES,
    /**
     * A permanent that was a creature on the battlefield is put into this permanent's
     * controller's graveyard. This is keyed to the graveyard owner rather than the creature's
     * last controller and uses the permanent's last-known battlefield type.
     */
    ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
    /**
     * A creature <b>or planeswalker</b> you control dies (is put into a graveyard from the
     * battlefield). Superset of {@link #ON_ALLY_CREATURE_DIES}; a permanent that is both a
     * creature and a planeswalker still fires this slot only once. Ajani's Last Stand.
     */
    ON_ALLY_CREATURE_OR_PLANESWALKER_DIES,
    ON_DAMAGED_CREATURE_DIES,
    ON_COMBAT_DAMAGE_TO_PLAYER,
    ON_COMBAT_DAMAGE_TO_CREATURE,
    /** Triggers once per combat damage step when the source deals combat damage to one or more blockers. */
    ON_COMBAT_DAMAGE_TO_BLOCKING_CREATURE,
    ON_DAMAGE_TO_PLAYER,
    ON_ATTACK,
    ON_BECOMES_BLOCKED,
    /** Triggers once per attacking creature the controller controls that ends up unblocked
     *  ("Whenever this creature attacks and isn't blocked"). Fires during the declare-blockers
     *  step once blocks are locked in — after the defender declares blockers, or immediately when
     *  the defender has no possible blockers. Player-affecting effects (e.g. a discard) read the
     *  defending player from the stack entry's (non-targeting) {@code targetId}. Checked in
     *  {@code CombatBlockService}. Used by Abyssal Nightstalker. */
    ON_ATTACKS_UNBLOCKED,
    /** Aura slot: triggers when the creature this aura is attached to attacks and isn't blocked
     *  ("Whenever enchanted creature attacks and isn't blocked"). Fires during the declare-blockers
     *  step alongside {@code ON_ATTACKS_UNBLOCKED}; the stack entry bakes the enchanted attacker as
     *  the (non-targeting) {@code sourcePermanentId} and the defending player as the {@code targetId}.
     *  Checked in {@code CombatBlockService}. Used by Cloak of Confusion. */
    ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED,
    DRAW_TRIGGERED,
    EACH_DRAW_TRIGGERED,
    /** Marker slot: the controller may skip their turn-based draw-step draw. Detected by presence
     *  (not effect type) in {@code StepTriggerService.handleDrawStep}, which offers the controller a
     *  may-ability to replace the draw. Used by Fasting and Island Sanctuary. */
    MAY_SKIP_DRAW_STEP_DRAW,
    /** Replacement ability of a card in its owner's graveyard that may replace a card draw by
     *  milling a fixed number of cards and returning that card to its owner's hand. */
    GRAVEYARD_DRAW_REPLACEMENT,
    END_STEP_TRIGGERED,
    /** End-step trigger fired from a card sitting in its owner's graveyard ("At the beginning of the
     *  end step, if this card is in your graveyard …"). Fires at EVERY end step, not just the card
     *  owner's, and is scanned across all players' graveyards by
     *  {@code StepTriggerService.handleEndStepTriggers}. Supports an intervening-if
     *  {@link com.github.laxika.magicalvibes.model.effect.ConditionalEffect} gate checked at trigger
     *  time. Used by Krovikan Horror. Contrast {@link #GRAVEYARD_UPKEEP_TRIGGERED}. */
    GRAVEYARD_END_STEP_TRIGGERED,
    /** End-step trigger fired from a card in its owner's graveyard during that owner's end step. */
    GRAVEYARD_CONTROLLER_END_STEP_TRIGGERED,
    CONTROLLER_END_STEP_TRIGGERED,
    /** "At the beginning of each opponent's end step" — fires during the end step of any player who
     *  is an opponent of this permanent's controller (i.e. not the controller's own end step).
     *  Checked in {@code StepTriggerService.handleEndStepTriggers}, which bakes the end-step player
     *  (that opponent) into the stack entry's {@code targetId} so an intervening-if
     *  {@link com.github.laxika.magicalvibes.model.effect.ConditionalEffect} can gate on "that player"
     *  (e.g. {@code EndStepPlayerDidntCastCreatureSpell}). Used by Predatory Advantage. */
    OPPONENT_END_STEP_TRIGGERED,
    ON_CONTROLLER_DRAWS,
    /** Triggers when the controller draws their second card of the turn. */
    ON_CONTROLLER_DRAWS_SECOND_CARD,
    /** Triggers when this card's owner draws their second card of the turn while this card is in
     *  their graveyard. Checked in {@code DrawService}. */
    GRAVEYARD_ON_CONTROLLER_DRAWS_SECOND_CARD,
    ON_OPPONENT_DRAWS,
    /** Triggers whenever the player enchanted by this Aura draws a card. */
    ON_ENCHANTED_PLAYER_DRAWS,
    ON_OPPONENT_DISCARDS,
    ON_CONTROLLER_FORAGES,
    /** Whenever the controller discards a card ("whenever you discard a card"). Fires on the discarding
     *  player's own battlefield in {@code TriggerCollectionService.checkDiscardTriggers}. Used by Necropotence. */
    ON_CONTROLLER_DISCARDS,
    /** Triggers once for a discard event in which the controller discards one or more cards. */
    ON_CONTROLLER_DISCARD_EVENT,
    /** Triggers whenever this permanent's controller scries. Checked by
     *  {@code TriggerCollectionService.checkScryTriggers}. */
    ON_CONTROLLER_SCRIES,
    /** Triggers when this permanent's controller investigates for the first time each turn. */
    ON_CONTROLLER_INVESTIGATES,
    /** Triggers whenever this permanent's controller surveils. Checked by
     *  {@code TriggerCollectionService.checkSurveilTriggers}. */
    ON_CONTROLLER_SURVEILS,
    ON_CONTROLLER_BENDS,
    /** Triggers whenever this permanent's controller collects evidence. */
    ON_CONTROLLER_COLLECTS_EVIDENCE,
    /** Triggers whenever this permanent's controller completes a discover action. */
    ON_CONTROLLER_DISCOVERS,
    /** Triggers when this card is discarded for any reason ("When you discard this card, …").
     *  Unlike {@link #ON_SELF_DISCARDED_BY_OPPONENT}, fires on self-discard and opponent-caused discard.
     *  Checked in {@code TriggerCollectionService.checkDiscardTriggers}. Used by Edgar's Awakening. */
    ON_SELF_DISCARDED,
    ON_SELF_DISCARDED_BY_OPPONENT,
    ON_ANY_PLAYER_TAPS_LAND,
    /** "Whenever you tap a creature for mana"; checked on the tapping player's battlefield. */
    ON_CONTROLLER_TAPS_CREATURE_FOR_MANA,
    ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
    ON_ALLY_PERMANENT_SACRIFICED,
    /** Triggers whenever an opponent gains control of a permanent from this permanent's controller. */
    ON_OPPONENT_GAINS_CONTROL_OF_YOUR_PERMANENT,
    /** Triggers whenever an opponent sacrifices a permanent. */
    ON_OPPONENT_PERMANENT_SACRIFICED,
    /** Triggers whenever any player sacrifices a permanent. */
    ON_ANY_PERMANENT_SACRIFICED,
    /** Triggers whenever an opponent sacrifices a nontoken permanent. */
    ON_OPPONENT_NONTOKEN_PERMANENT_SACRIFICED,
    /** Global watcher: triggers whenever any player sacrifices a creature ("Whenever a player
     *  sacrifices a creature"). Fires on every permanent with this slot across all battlefields, once
     *  per sacrificed creature (last-known info decides creature-ness). The trigger belongs to the
     *  scanning permanent's controller; a wrapped {@code MayEffect(PutCountersOnSourceEffect)} resolves
     *  onto that permanent (like Scavenger Drake's {@code ON_ANY_CREATURE_DIES}). Checked in
     *  {@code TriggerCollectionService.checkAnyCreatureSacrificedTriggers}, fired from the two sacrifice
     *  choke points ({@code DestructionSupport.sacrificeAndLog} for edict/chosen sacrifices,
     *  {@code checkAllyPermanentSacrificedTriggers} for sacrifice-self / sacrifice-as-cost). Used by
     *  Thraximundar. */
    ON_ANY_CREATURE_SACRIFICED,
    /** Triggered when a player chooses one or more targets for a spell or ability. */
    ON_ANY_PLAYER_CHOOSES_TARGETS,
    ON_BECOMES_TARGET_OF_SPELL,
    /** Triggers when this permanent becomes the target of an Aura spell (any player's — the Aura's
     *  controller is irrelevant). Spells only; an Aura already on the battlefield being moved by an
     *  ability never triggers it. The trigger belongs to the targeted permanent's controller and goes
     *  on the stack with {@code sourcePermanentId} set. Checked in
     *  {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers}. Used by Fugitive Druid
     *  (pair with {@code DrawCardEffect}). */
    ON_BECOMES_TARGET_OF_AURA_SPELL,
    ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
    /** Triggers when this permanent becomes the target of an opponent-controlled spell, but not an ability. */
    ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY,
    /** Triggers when this permanent's controller becomes the target of a spell. */
    ON_CONTROLLER_BECOMES_TARGET_OF_SPELL,
    /** Triggers when the controller of a permanent with this slot becomes the target of a spell or
     * ability controlled by an opponent. The targeted player is not a permanent, so this is checked
     * from the player-target paths in {@code TriggerCollectionService}. */
    ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
    ON_ANY_CREATURE_DIES,
    ON_ALLY_NONTOKEN_CREATURE_DIES,
    ON_ANY_NONTOKEN_CREATURE_DIES,
    ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever an enchantment (any player's) is put into a graveyard from the battlefield.
     *  Fires for destroy, sacrifice, etc. Checked in {@code PermanentRemovalService} via
     *  {@code TriggerCollectionService.checkAnyEnchantmentPutIntoGraveyardFromBattlefieldTriggers}.
     *  Used by Femeref Enchantress (pair with {@code DrawCardEffect}). */
    ON_ANY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever an enchantment controlled by this permanent's controller is put into a
     *  graveyard from the battlefield. Checked in {@code PermanentRemovalService} via
     *  {@code TriggerCollectionService.checkAnyEnchantmentPutIntoGraveyardFromBattlefieldTriggers}. */
    ON_ALLY_ENCHANTMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a permanent (of any type) an opponent of the controller controls is put into
     *  a graveyard from the battlefield. Fires on permanents controlled by an opponent of the dying
     *  permanent's controller. Checked in {@code PermanentRemovalService.processGraveyardAndTriggers}
     *  via {@code TriggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers}. Used by
     *  Prince of Thralls. */
    ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a permanent (of any type) <em>owned</em> by a player other than the controller
     *  is put into a graveyard from the battlefield. Ownership-based, not control-based: a stolen
     *  permanent still counts for its owner. Checked in
     *  {@code PermanentRemovalService.processGraveyardAndTriggers} via
     *  {@code TriggerCollectionService.checkOtherPlayerOwnedPermanentPutIntoGraveyardTriggers}.
     *  Used by Kothophed, Soul Hoarder. */
    ON_OTHER_PLAYER_OWNED_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a permanent (of any type, any player's) is put into a graveyard from the
     *  battlefield. Fires for destroy, sacrifice, state-based death, etc. The source permanent never
     *  sees its own death (it has already left the battlefield), so "other than ~" is implicit.
     *  Supports gating via {@code TriggeringCardConditionalEffect} on the dead permanent's card or
     *  {@code TriggeringPermanentControllerConditionalEffect} on its controller.
     *  Checked in {@code PermanentRemovalService.processGraveyardAndTriggers} via
     *  {@code TriggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers}. Used by Yomiji,
     *  Who Bars the Way (pair with {@code ReturnTriggeringCardToOwnerHandEffect}). */
    ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a nontoken permanent of any type is put into any player's graveyard from
     *  the battlefield. Fires for the graveyard owner and excludes tokens. */
    ON_ANY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a nontoken permanent is put into the controller's graveyard from the
     *  battlefield. Fires only for the graveyard owner, including permanents of any type, and
     *  excludes tokens. Checked in {@code TriggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers}.
     *  Used by Jinxed Ring. */
    ON_ALLY_NONTOKEN_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a permanent of any type, including a token, is put into the controller's
     *  graveyard from the battlefield. Fires only for the graveyard owner. */
    ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a permanent (of any type, any player's) is put into an <em>opponent's</em>
     *  graveyard from the battlefield — the graveyard's owner, not the permanent's controller, is
     *  what makes it an opponent's graveyard. The triggered ability's {@code targetId} is baked to
     *  that graveyard owner, so a {@code LoseLifeEffect(N, TARGET_PLAYER)} reads as "that player
     *  loses N life". Checked in {@code PermanentRemovalService.processGraveyardAndTriggers} via
     *  {@code TriggerCollectionService.checkAnyPermanentPutIntoGraveyardTriggers}. Used by Patron of
     *  the Nezumi. */
    ON_PERMANENT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers when a land the controller owns is put into their graveyard from the battlefield
     *  because of a spell or ability an opponent controls (Sacred Ground). Fires only on permanents
     *  the graveyard owner controls. */
    ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT,
    /** Triggers when a noncreature permanent the controller controls is destroyed by a spell or
     *  ability an opponent controls. Used by Karmic Justice. */
    ON_ALLY_NONCREATURE_PERMANENT_DESTROYED_BY_OPPONENT,
    /** Triggers whenever a land card the controller owns is put into their graveyard from anywhere
     *  (battlefield, hand, library, stack, exile). Fires on permanents the graveyard owner controls.
     *  Checked in {@code GraveyardService.addCardToGraveyard} (the single zone→graveyard choke point)
     *  via {@code TriggerCollectionService.checkLandPutIntoGraveyardFromAnywhereTriggers}. Used by
     *  Countryside Crusher. */
    ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers whenever a land card is put into the controller's graveyard from their library.
     *  Fires on permanents the graveyard owner controls. */
    ON_ALLY_LAND_CARD_MILLED,
    /** Triggers once for each library-to-graveyard event in which one or more creature cards enter the
     *  controller's graveyard. */
    ON_ALLY_CREATURE_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
    /** Triggers once for each library-to-graveyard event in which one or more cards enter the
     *  controller's graveyard. */
    ON_ALLY_CARDS_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
    /** Triggers whenever a creature card is put into any player's graveyard from a library. */
    ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_LIBRARY,
    /** Triggers whenever a non-token card is put into the controller's graveyard from anywhere. */
    ON_ALLY_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers whenever a creature card the controller owns is put into their graveyard from anywhere
     *  (battlefield, hand, library, stack, exile). Fires on permanents the graveyard owner controls.
     *  Uses the card's printed types (not battlefield creature-ness), so tokens never trigger and a
     *  creature card that was a noncreature permanent still does. Checked in
     *  {@code GraveyardService.addCardToGraveyard} via
     *  {@code TriggerCollectionService.checkCreatureCardPutIntoGraveyardFromAnywhereTriggers}.
     *  Used by Soulcipher Board. */
    ON_ALLY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers whenever a creature card enters any graveyard from a zone other than the battlefield. */
    ON_ANY_CREATURE_CARD_PUT_INTO_GRAVEYARD_FROM_NONBATTLEFIELD,
    /** Triggers whenever a nontoken permanent card the controller owns is put into their graveyard
     *  from anywhere (battlefield, hand, library, stack, exile). Fires on permanents the graveyard
     *  owner controls. Checked in {@code GraveyardService.addCardToGraveyard} via
     *  {@code TriggerCollectionService.checkPermanentCardPutIntoGraveyardFromAnywhereTriggers}. */
    ON_ALLY_PERMANENT_CARD_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers whenever a creature card is put into an opponent's graveyard from anywhere
     *  (battlefield, hand, library, stack, exile). Fires on permanents controlled by an opponent of
     *  the graveyard owner. Uses the card's printed types, so tokens never trigger. Checked in
     *  {@code GraveyardService.addCardToGraveyard} via
     *  {@code TriggerCollectionService.checkCreatureCardPutIntoGraveyardFromAnywhereTriggers}.
     *  Used by Profane Memento. */
    ON_CREATURE_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
    /**
     * Triggers whenever a non-token card is put into an opponent's graveyard from anywhere
     * (battlefield, hand, library, stack, exile). Fires on permanents controlled by an opponent of
     * the graveyard owner. The trigger's target context is the graveyard owner.
     */
    ON_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers whenever a land (any player's) is put into a graveyard from the battlefield. Fires
     *  for destroy, sacrifice, etc. Checked in {@code PermanentRemovalService} via
     *  {@code TriggerCollectionService.checkAnyLandPutIntoGraveyardFromBattlefieldTriggers}. Used by
     *  Dingus Egg (pair with {@code DealDamageToPlayersEffect(2, TRIGGERING_PERMANENT_CONTROLLER)}). */
    ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a black card is put into an opponent's graveyard from anywhere
     *  (battlefield, hand, library, stack, exile). Fires on permanents controlled by an opponent
     *  of the graveyard owner. Checked in {@code GraveyardService.addCardToGraveyard} (the single
     *  zone→graveyard choke point) via
     *  {@code TriggerCollectionService.checkBlackCardPutIntoOpponentGraveyardFromAnywhereTriggers}.
     *  Used by Compost. */
    ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers on a permanent whenever an Aura becomes attached to it — both when an Aura spell
     *  resolves onto it and when an already-resolved Aura is moved onto it. Queued as a
     *  non-targeting triggered ability whose {@code sourcePermanentId} is the enchanted permanent,
     *  controlled by that permanent's controller (so it fires for an opponent's Aura too).
     *  Checked in {@code TriggerCollectionService.checkAuraAttachedTriggers}. Used by Brood Keeper. */
    ON_AURA_ATTACHED_TO_SELF,
    /** Triggers whenever an Aura controlled by this permanent's controller becomes attached to a creature controlled by that player. */
    ON_ALLY_AURA_ATTACHED_TO_ALLY_CREATURE,
    /** Triggers on an Equipment whenever it becomes attached to a permanent. */
    ON_EQUIPMENT_ATTACHED,
    ON_ENCHANTED_PERMANENT_TAPPED,
    /** Triggers whenever a permanent the controller controls becomes tapped. Fires on every
     *  permanent with this slot on the tapped permanent's controller's battlefield. Wrap the
     *  effect in {@code TriggeringPermanentConditionalEffect} to filter by the tapped permanent
     *  (e.g. Judge of Currents — "whenever a Merfolk you control becomes tapped"). Checked in
     *  {@code TriggerCollectionService.checkEnchantedPermanentTapTriggers}, driven by the same
     *  tap-event call sites as {@code ON_ENCHANTED_PERMANENT_TAPPED}. */
    ON_ALLY_PERMANENT_BECOMES_TAPPED,
    /** Triggers whenever this creature is tapped to pay the crew cost of a Vehicle. The
     *  triggering Vehicle is stored on the resulting stack entry for effects that refer to it.
     *  Checked from {@code CrewCostHandler}. */
    ON_CREWS_VEHICLE,
    /** Triggers whenever this permanent becomes untapped (transitions from tapped to untapped),
     *  from any source — the untap step, or an untap effect. Fires only on the permanent that
     *  became untapped. Driven from the untap call sites via
     *  {@code TriggerCollectionService.checkBecomesUntappedTriggers}
     *  ({@code UntapStepService} and {@code TapUntapSupport.untapPermanent}). The trigger is queued
     *  with the untapped permanent as its {@code sourcePermanentId}; targeted abilities choose
     *  their targets before being put on the stack, including abilities whose effect is optional. */
    ON_SELF_BECOMES_UNTAPPED,
    /** Triggers whenever a permanent the controller controls becomes untapped (transitions from
     *  tapped to untapped), from any source — the untap step or an untap effect. Fires on every
     *  permanent with this slot on the untapped permanent's controller's battlefield (including the
     *  untapped permanent itself). Wrap the effect in {@code TriggeringPermanentConditionalEffect}
     *  to filter by the untapped permanent (e.g. "whenever a Merfolk you control becomes untapped").
     *  Checked in {@code TriggerCollectionService.checkBecomesUntappedTriggers}, driven from the same
     *  untap call sites as {@code ON_SELF_BECOMES_UNTAPPED}. Used by Wake Thrasher
     *  ({@code BoostSelfEffect(1, 1)}). */
    ON_ALLY_PERMANENT_BECOMES_UNTAPPED,
    /** Triggers once during the controller's untap step when one or more of their permanents untap. */
    ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP,
    /** Triggers whenever any permanent becomes untapped, regardless of its controller. The
     *  untapped permanent is recorded on the triggered entry as the non-targeting event permanent. */
    ON_ANY_PERMANENT_BECOMES_UNTAPPED,
    /** Triggers whenever this creature becomes renowned (CR 702.112b) — i.e. when a {@code RenownEffect}
     *  actually flips it from not-renowned to renowned. A creature that is already renowned never fires
     *  this again (CR 702.112c). Driven from {@code RenownEffectHandler} via
     *  {@code TriggerCollectionService.checkBecomesRenownedTriggers}; queued as a non-targeting triggered
     *  ability whose {@code sourcePermanentId} is the newly renowned creature. Used by Relic Seeker
     *  ({@code MayEffect(SearchLibraryEffect(Equipment))}). */
    ON_SELF_BECOMES_RENOWNED,
    /** Triggers whenever a creature the controller controls becomes renowned (CR 702.112b) — fires on
     *  every permanent with this slot on the newly renowned creature's controller's battlefield,
     *  including that creature itself. Wrap the effect in {@code TriggeringPermanentConditionalEffect}
     *  to filter by the renowned creature. Driven from {@code RenownEffectHandler} via
     *  {@code TriggerCollectionService.checkBecomesRenownedTriggers}, alongside
     *  {@code ON_SELF_BECOMES_RENOWNED}. Used by Valeron Wardens ({@code DrawCardEffect(1)}). */
    ON_ALLY_CREATURE_BECOMES_RENOWNED,
    /** Triggers whenever this creature becomes monstrous for the first time. */
    ON_SELF_BECOMES_MONSTROUS,
    /** Triggers whenever this permanent phases out — from the untap step's phasing turn-based action
     *  (CR 702.26a) or from an effect that phases it out. Fires only on the permanent that phased
     *  out, driven from {@code PhasingService} via
     *  {@code TriggerCollectionService.checkPhasesOutTriggers}. The trigger is collected before the
     *  permanent leaves the battlefield, because abilities that trigger on phasing out look back in
     *  time (CR 603.10b) — the permanent is "treated as though it does not exist" once phased out
     *  (CR 702.26b). Queued as a non-targeting triggered ability whose {@code sourcePermanentId} is
     *  the phased-out permanent. Used by Teferi's Imp ({@code DiscardEffect}). */
    ON_SELF_PHASES_OUT,
    /** Triggers whenever this permanent phases in during its controller's untap step (CR 702.26a).
     *  Fires only on the permanent that phased in, driven from {@code PhasingService} via
     *  {@code TriggerCollectionService.checkPhasesInTriggers} after it is back on the battlefield.
     *  Queued as a non-targeting triggered ability whose {@code sourcePermanentId} is the phased-in
     *  permanent. Used by Teferi's Imp ({@code DrawCardEffect}). */
    ON_SELF_PHASES_IN,
    /** Triggers whenever a permanent an <em>opponent</em> of the controller controls becomes tapped.
     *  Fires on every permanent with this slot controlled by a player other than the tapped
     *  permanent's controller. Wrap the effect in {@code TriggeringPermanentConditionalEffect} to
     *  filter by the tapped permanent (e.g. Thoughtleech — "whenever an Island an opponent controls
     *  becomes tapped"). Checked in {@code TriggerCollectionService.checkEnchantedPermanentTapTriggers},
     *  driven by the same tap-event call sites as {@code ON_ENCHANTED_PERMANENT_TAPPED} — so it fires
     *  on any tap (for mana or forced, e.g. Icy Manipulator), not just taps for mana. */
    ON_OPPONENT_PERMANENT_BECOMES_TAPPED,
    /** Triggers whenever the permanent this aura is attached to is dealt damage (combat or non-combat).
     *  Fires on the aura permanent; the dealt damage amount is passed via {@code TriggerContext.DamageToCreature}. */
    ON_ENCHANTED_CREATURE_DEALT_DAMAGE,
    /** Triggers whenever the creature this aura is attached to deals damage (combat or non-combat) to the
     *  aura's controller — i.e. "whenever enchanted creature deals damage to you" (Backfire). Because the
     *  aura sits on its controller's battlefield, the trigger is scanned on the damaged player's battlefield
     *  in {@code TriggerCollectionService.checkEnchantedCreatureDealtDamageToControllerReflectTriggers}, so it
     *  fires only when the damaged player is the aura's controller. Reuses
     *  {@code EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect} for resolution. */
    ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU,
    /** Triggers whenever the creature this Aura is attached to deals damage to anything. */
    ON_ENCHANTED_CREATURE_DEALS_DAMAGE,
    /** Triggers whenever the creature this Aura is attached to deals damage to another creature.
     *  The damaged creature is captured as the non-targeting {@code targetId} of the queued
     *  ability. Checked from both combat and noncombat creature-damage paths. */
    ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE,
    ON_EQUIPPED_CREATURE_DIES,
    /** Triggers on an Equipment whenever the creature it is attached to deals combat damage to anything. */
    ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
    /** Triggers on an Equipment whenever the creature it is attached to transforms, in either
     *  direction. Fired by {@code AnimationSupport.fireEquipmentTransformTriggers} right after the
     *  equipped creature's own transform triggers. Used by Neglected Heirloom. */
    ON_EQUIPPED_CREATURE_TRANSFORMS,
    /** Triggers whenever a permanent controlled by this permanent's controller transforms. */
    ON_ALLY_PERMANENT_TRANSFORMS,
    /** Triggers whenever the game changes between day and night. */
    ON_DAY_NIGHT_CHANGE,
    /** Triggers whenever the game changes between day and night while this card is in its controller's graveyard. */
    GRAVEYARD_ON_DAY_NIGHT_CHANGE,
    /** Triggers whenever a permanent controlled by this permanent's controller enters transformed. */
    ON_ALLY_PERMANENT_ENTERS_TRANSFORMED,
    ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
    ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
    /** Triggers whenever a land the controller controls enters the battlefield.
     *  Checked in {@code BattlefieldEntryService.checkAllyLandEntersTriggers}. */
    ON_ALLY_LAND_ENTERS_BATTLEFIELD,
    ON_OPPONENT_CREATURE_DIES,
    ON_DEALT_DAMAGE,
    /** Triggers whenever this creature is dealt combat damage. The amount of combat damage dealt
     *  is snapshotted onto the triggered ability's event value. */
    ON_COMBAT_DAMAGE_TO_SELF,
    ON_OPENING_HAND_REVEAL,
    /** Special actions offered while a player is deciding whether to mulligan. */
    MULLIGAN_ACTION,
    ON_OPPONENT_LOSES_LIFE,
    ON_OPPONENT_SHUFFLES_LIBRARY,
    /** Triggers whenever an opponent of this permanent's controller searches their own library
     *  ("Whenever an opponent searches their library"). Fired from the unified library-search choke
     *  point ({@code LibrarySearchSupport.performLibrarySearch}) by
     *  {@code LibrarySearchTriggerHelper}, which bakes the searching player as the triggered
     *  ability's {@code targetId} so {@code TARGET_PLAYER}-scoped effects act on them. A search
     *  prevented by Leonin Arbiter never happens and does not trigger this. Used by
     *  Ob Nixilis, Unshackled. */
    ON_OPPONENT_SEARCHES_LIBRARY,
    ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
    /** Triggers during the draw step of the enchanted permanent's controller ("At the beginning of
     *  the draw step of enchanted creature's controller, that player draws an additional card").
     *  Checked in {@code StepTriggerService.handleDrawStepTriggers}; bakes that player as
     *  {@code targetId}. Used by Righteous Authority. */
    ENCHANTED_PERMANENT_CONTROLLER_DRAW_TRIGGERED,
    /** Triggers during the end step of the enchanted permanent's controller ("At the beginning of
     *  your end step" on an ability granted to the enchanted permanent). Checked in
     *  {@code StepTriggerService.handleEndStepTriggers}. Used by Nettlevine Blight. */
    ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED,
    ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
    /** Aura slot for player-enchanting Curses: "At the beginning of each end step, enchanted player …".
     *  Fires at EVERY end step (any player's turn), unlike {@link #ENCHANTED_PLAYER_UPKEEP_TRIGGERED}
     *  which is gated to the enchanted player's own upkeep. The enchanted player's id is baked as the
     *  (non-targeting) {@code targetId} so a {@code MillEffect(TARGET_PLAYER)} lands on them. Checked in
     *  {@code StepTriggerService.handleEndStepTriggers}. Used by Fraying Sanity. */
    ENCHANTED_PLAYER_END_STEP_TRIGGERED,
    ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
    /** "Whenever an enchantment enters under your control" (excludes this permanent). Filter by
     *  subtype with a {@code TriggeringCardConditionalEffect}. Checked in
     *  {@code TriggerCollectionService.checkAllyEnchantmentEntersTriggers}. Used by Trial of Solidarity. */
    ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
    ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
    /** Triggers whenever this creature or another creature enters the battlefield from the
     *  controller's graveyard. Checked in {@code BattlefieldEntryService.checkEntersFromGraveyardTriggers}
     *  after a creature enters, using the {@code enteredFromGraveyardOwnerId} flag on the entering
     *  permanent. Targeting effects use the any-target pipeline
     *  ({@code EnteringPermanentAnyTargetTrigger} interactions); entering-creature attachment effects
     *  use the enter-trigger collector's baked-in creature instead. Used by Flayer of the Hatebound
     *  and Dredging Claw. */
    ON_CREATURE_ENTERS_FROM_GRAVEYARD,
    /** "Whenever this creature or another permanent enters from a graveyard" — fires for ANY permanent
     *  (not just creatures) entering the battlefield from ANY graveyard, checked via the
     *  {@code enteredFromGraveyardOwnerId} flag. Queues the resolved effects as a non-targeting stack
     *  entry for the source's controller. Used by River Kelpie. */
    ON_PERMANENT_ENTERS_FROM_GRAVEYARD,
    /** Triggers whenever a permanent controlled by this permanent's controller enters the
     *  battlefield directly from exile. Checked after the permanent enters, using the
     *  {@code enteredFromExile} flag on the entering permanent. */
    ON_PERMANENT_ENTERS_FROM_EXILE,
    /** "When this creature enters from a graveyard" — fires only for the entering permanent itself
     *  (not for other permanents), checked via the {@code enteredFromGraveyardOwnerId} flag in
     *  {@code TriggerCollectionService.checkSelfEntersFromGraveyardTriggers}. A targeting effect
     *  chooses its target as the trigger goes on the stack (CR 603.3b) through the shared
     *  {@code ETBTokenTargetTrigger} pipeline, using the card's {@code target(...)} filter; a
     *  non-targeting effect is queued directly. Deliberately separate from
     *  {@code ON_ENTER_BATTLEFIELD} so a normal cast never asks for a target.
     *  Used by Treacherous Pit-Dweller. */
    ON_SELF_ENTERS_FROM_GRAVEYARD,
    /** "Whenever a player puts a permanent onto the battlefield" — fires for EVERY permanent entering
     *  under ANY player's control (including the source itself), once per entering permanent, for every
     *  permanent on any battlefield carrying this slot. Checked in
     *  {@code TriggerCollectionService.checkAnyPermanentEntersTriggers}. The entering permanent's
     *  controller is baked in as the non-targeting {@code targetId}, so a player-directed effect
     *  (e.g. {@code SacrificePermanentsEffect(…, SacrificeRecipient.TARGET_PLAYER)}) acts on "that
     *  player". The entering permanent's id / card id are stamped on {@code triggeringPermanentId} /
     *  {@code triggeringCardId} for effects that act on "that permanent" or its name (Eye of
     *  Singularity). Filter which permanents trigger it with a {@code TriggeringCardConditionalEffect}
     *  wrapper. Used by Nature's Wrath / Eye of Singularity. */
    ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
    ON_CONTROLLER_GAINS_LIFE,
    /** Triggers whenever an opponent of this permanent's controller gains life. */
    ON_OPPONENT_GAINS_LIFE,
    /** Triggers whenever this permanent's controller gets one or more energy counters. */
    ON_CONTROLLER_GETS_ENERGY,
    /** Triggers whenever this permanent's controller wins a coin flip. */
    ON_CONTROLLER_WINS_COIN_FLIP,
    /** Triggers whenever this permanent's controller loses a coin flip. */
    ON_CONTROLLER_LOSES_COIN_FLIP,
    ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE,
    ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
    /** Triggers whenever one or more matching creatures deal combat damage to a player or battle. */
    ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
    /** Triggers whenever any creature deals combat damage to one of this permanent's controller's opponents. */
    ON_ANY_CREATURE_COMBAT_DAMAGE_TO_OPPONENT,
    ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
    ON_OPPONENT_CREATURE_CARD_MILLED,
    ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD,
    /** Triggers when this card is put into its owner's graveyard from their library (milled).
     *  Checked per-card inside {@code GraveyardService.resolveMillPlayer}. */
    ON_SELF_MILLED,
    /** Triggers when this card is put into a graveyard from anywhere (battlefield, hand, library,
     *  stack, exile). Checked for every card entering a graveyard in
     *  {@code GraveyardService.addCardToGraveyard}, which is the single choke point for all
     *  zone→graveyard transitions. Fires as a triggered ability (the card enters the graveyard
     *  first). Used by Purity ("shuffle it into its owner's library"). */
    ON_SELF_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
    /** Triggers when this card is put into a graveyard specifically from the battlefield (i.e. "dies"
     *  for a permanent). Checked in {@code GraveyardService.addCardToGraveyard} only when the source
     *  zone is {@code Zone.BATTLEFIELD}. Fires as a triggered ability (the card enters the graveyard
     *  first). Used by Spreading Algae ("return it to its owner's hand"). */
    ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers once when one or more creatures the controller controls are declared as attackers.
     *  Unlike ON_ATTACK (which fires per creature), this fires exactly once per combat. */
    ON_ALLY_CREATURES_ATTACK,
    /** Triggers once for each player directly attacked by one or more creatures the controller
     *  controls. Attacking a planeswalker does not count. Checked in
     *  {@code CombatAttackService.declareAttackers}. */
    ON_ALLY_CREATURES_ATTACK_PLAYER,
    /** State-triggered abilities (MTG rule 603.8). Checked after SBAs; fire once onto the
     *  stack and don't retrigger while the ability is already on the stack. */
    STATE_TRIGGERED,
    /** Saga chapter I ability (MTG rule 714). Triggers when the first lore counter is placed. */
    SAGA_CHAPTER_I,
    /** Saga chapter II ability (MTG rule 714). Triggers when the second lore counter is placed. */
    SAGA_CHAPTER_II,
    /** Saga chapter III ability (MTG rule 714). Triggers when the third lore counter is placed. */
    SAGA_CHAPTER_III,
    /** Saga chapter IV ability. Triggers when the fourth lore counter is placed. */
    SAGA_CHAPTER_IV,
    /** Saga chapter V ability. Triggers when the fifth lore counter is placed. */
    SAGA_CHAPTER_V,
    /** Triggers at the beginning of combat on the controller's turn.
     *  Checked in {@code StepTriggerService.handleBeginningOfCombatTriggers}. */
    BEGINNING_OF_COMBAT_TRIGGERED,
    /** Triggers at the beginning of combat on this card's owner's turn while the card is in their
     *  graveyard. Checked in {@code StepTriggerService.handleBeginningOfCombatTriggers}. */
    GRAVEYARD_BEGINNING_OF_COMBAT_TRIGGERED,
    /** Triggers at the beginning of each combat (any player's turn), not only the controller's.
     *  Checked in {@code StepTriggerService.handleBeginningOfCombatTriggers} by scanning all
     *  battlefields. Used by Majestic Myriarch / Odric, Lunarch Marshal. */
    EACH_BEGINNING_OF_COMBAT_TRIGGERED,
    /** Triggers at the beginning of combat on each opponent's turn (never on the controller's).
     *  Checked in {@code StepTriggerService.handleBeginningOfCombatTriggers} by scanning every
     *  battlefield other than the active player's. Used by Sentinel of the Eternal Watch. */
    OPPONENT_BEGINNING_OF_COMBAT_TRIGGERED,
    /** "At end of combat, …" — triggers as the end of combat step begins (CR 511.2), for every
     *  combat phase on any player's turn, not only the controller's. Fires on every permanent
     *  carrying this slot across all battlefields. Checked in
     *  {@code StepTriggerService.handleEndOfCombatTriggers}, called from
     *  {@code TurnProgressionService} right after {@code CombatService.clearCombatState}. The
     *  trigger is queued as a non-targeting triggered ability. Used by Heat Stroke. */
    END_OF_COMBAT_TRIGGERED,
    /** Triggers at the beginning of the active player's precombat main phase on the
     *  controller's turn. Checked in {@code StepTriggerService.handlePrecombatMainTriggers}. */
    PRECOMBAT_MAIN_TRIGGERED,
    /** Triggers at the beginning of each player's first main phase (any player's turn), not only
     *  the controller's. Checked in {@code StepTriggerService.handlePrecombatMainTriggers} by
     *  scanning all battlefields; the trigger is controlled by the source's controller while the
     *  active player is carried as the stack entry's target. Used by Eladamri's Vineyard. */
    EACH_PRECOMBAT_MAIN_TRIGGERED,
    /** Triggers at the beginning of each of the controller's postcombat main phases.
     *  Checked in {@code StepTriggerService.handlePostcombatMainTriggers}. */
    POSTCOMBAT_MAIN_TRIGGERED,
    /** Triggers whenever a creature an opponent controls is dealt damage (combat or non-combat).
     *  Fires on the permanent with this slot, not on the damaged creature. Scans all battlefields
     *  for permanents with this slot whose controller is different from the damaged creature's controller. */
    ON_OPPONENT_CREATURE_DEALT_DAMAGE,
    ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_DAMAGE,
    /** Triggers whenever an opponent's creature is dealt excess noncombat damage. */
    ON_OPPONENT_CREATURE_DEALT_EXCESS_NONCOMBAT_DAMAGE,
    /** Triggers whenever any creature (yours or an opponent's) is dealt damage (combat or non-combat).
     *  Fires on the permanent with this slot, not on the damaged creature. Scans all battlefields;
     *  the queued stack entry targets the damaged creature (e.g. Death Pits of Rath). */
    ON_ANY_CREATURE_DEALT_DAMAGE,
    /** Triggers whenever a creature this permanent's controller controls (matching the effect's
     *  source filter) deals damage — combat or non-combat — to a creature. Fires on the permanent
     *  with this slot, not on the damaged creature. Scans all battlefields; the reflected damage is
     *  dealt by the damage-source creature to the damaged creature's controller (e.g. Greatbow Doyen). */
    ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
    /** Triggers when the controller casts a spell matching the filter, while this card is in
     *  the controller's graveyard.  Checked per-card inside
     *  {@code TriggerCollectionService.checkSpellCastTriggers}. */
    GRAVEYARD_ON_CONTROLLER_CASTS_SPELL,
    /** Triggers whenever the controller surveils, while this card is in the controller's
     *  graveyard. Checked per-card inside {@code TriggerCollectionService.checkSurveilTriggers}. */
    GRAVEYARD_ON_CONTROLLER_SURVEILS,
    /** Triggers whenever the controller activates an exhaust ability, while this card is in the
     *  controller's graveyard. Checked per-card inside
     *  {@code TriggerCollectionService.checkControllerActivatesExhaustAbilityTriggersFromGraveyard}. */
    GRAVEYARD_ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY,
    /** Triggers whenever the controller sacrifices a permanent, while this card is in the
     *  controller's graveyard. Checked per-card inside
     *  {@code TriggerCollectionService.checkGraveyardAllyPermanentSacrificedTriggers}. */
    GRAVEYARD_ON_CONTROLLER_PERMANENT_SACRIFICED,
    /** Triggers once for each creature card that leaves an opponent's graveyard, while this card is
     *  in its owner's graveyard. Fired per leaving card from
     *  {@code GraveyardService.notifyCardLeftGraveyard} (and the bulk clear path), which scans the
     *  graveyards of every opponent of the graveyard the card left. Non-targeting.
     *  Used by Erebos's Titan. */
    GRAVEYARD_ON_CREATURE_CARD_LEAVES_OPPONENT_GRAVEYARD,
    /** Triggers when this card leaves its owner's graveyard and is put into its owner's hand. */
    GRAVEYARD_ON_SELF_RETURNED_TO_HAND,
    /** Triggers when the controller casts a spell matching the filter, while this card is in
     *  the controller's command zone (Eminence — e.g. Edgar Markov). Checked per-card inside
     *  {@code TriggerCollectionService.checkSpellCastTriggers}. Pair with an intervening-if
     *  {@code SourceCardInCommandZone} so the ability fails if the card left the command zone. */
    COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL,
    /** Triggers whenever the controller of this permanent loses life (damage or direct life loss).
     *  Fires on the controller's own permanents. The amount is passed via TriggerContext.LifeLoss.
     *  Hooked into TriggerCollectionService.checkLifeLossTriggers(). Used by Lich's Mastery. */
    ON_CONTROLLER_LOSES_LIFE,
    /** Triggers whenever this permanent's controller pays life. */
    ON_CONTROLLER_PAYS_LIFE,
    /** Triggers when this permanent leaves the battlefield by any means (destruction, exile,
     *  bounce, sacrifice, tuck). Checked in PermanentRemovalService after removal. */
    ON_SELF_LEAVES_BATTLEFIELD,
    /** Triggers whenever another permanent leaves the battlefield by any means. */
    ON_ANOTHER_PERMANENT_LEAVES_BATTLEFIELD,
    /** Triggers whenever another creature (any player's) leaves the battlefield by any means
     *  (destruction, exile, bounce, sacrifice, tuck). Global watcher — fires on every permanent
     *  with this slot except the leaving creature itself. Checked in PermanentRemovalService via
     *  TriggerCollectionService.checkAnotherCreatureLeavesBattlefieldTriggers. Used by Extractor
     *  Demon ("you may have target player mill two cards" — a non-targeting MayEffect whose "may"
     *  and player target are resolved on the stack). */
    ON_ANOTHER_CREATURE_LEAVES_BATTLEFIELD,
    /** Triggers whenever another artifact controlled by this permanent's controller leaves the
     *  battlefield by any means (destruction, exile, bounce, sacrifice, tuck). Controller-scoped
     *  watcher — fires only on permanents sharing the leaving artifact's controller, except the
     *  leaving artifact itself. Checked in PermanentRemovalService via
     *  TriggerCollectionService.checkAnotherArtifactLeavesBattlefieldTriggers. Pairs with
     *  {@link #ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD} for "whenever another artifact you control
     *  enters or leaves the battlefield" (Sludge Strider). */
    ON_ANOTHER_ARTIFACT_LEAVES_BATTLEFIELD,
    /** Triggers whenever another nontoken artifact controlled by this permanent's controller is
     *  put into a graveyard or exile from the battlefield. */
    ON_ANOTHER_NONTOKEN_ARTIFACT_PUT_INTO_GRAVEYARD_OR_EXILE_FROM_BATTLEFIELD,
    /** Triggers whenever another creature controlled by this permanent's controller leaves the
     *  battlefield by any means (destruction, exile, bounce, sacrifice, tuck). Controller-scoped
     *  watcher — fires only on permanents sharing the leaving creature's controller, except the
     *  leaving creature itself. Checked in PermanentRemovalService via
     *  TriggerCollectionService.checkAllyCreatureLeavesBattlefieldTriggers. Used by Luminous
     *  Phantom ("you gain 1 life"). */
    ON_ALLY_CREATURE_LEAVES_BATTLEFIELD,
    ON_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING,
    ON_SELF_OR_ALLY_CREATURES_LEAVE_BATTLEFIELD_WITHOUT_DYING,
    /** Triggers whenever another permanent controlled by this permanent's controller leaves the
     *  battlefield during that controller's turn. */
    ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD_DURING_CONTROLLER_TURN,
    /** Triggers whenever an Aura or Equipment controlled by the same player is put into a
     *  graveyard from the battlefield. Checked in DeathTriggerService after the card enters
     *  the graveyard. Used by Tiana, Ship's Caretaker. */
    ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers when one or more creatures the controller controls are declared as attackers,
     *  while this card is in the controller's graveyard.  The attacker count is passed via
     *  xValue.  Checked in {@code CombatAttackService.declareAttackers}. */
    GRAVEYARD_ON_ALLY_CREATURES_ATTACK,
    /** Triggers when a creature the controller controls (matching the trigger's dealer predicate)
     *  deals combat damage to a player, while this card is in the controller's graveyard. Holds an
     *  {@link com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect}. Checked in
     *  {@code CombatDamageService.checkAllyCreatureCombatDamageToPlayerTriggers}. Used by Auntie's Snitch. */
    GRAVEYARD_ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
    /** Triggers from a graveyard when a creature is put into that card's owner's graveyard from the
     *  battlefield. Used by Recover cards such as Sun's Bounty. */
    GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers when combat damage is dealt to the controller or to a planeswalker they control,
     *  while this card is in the controller's graveyard. Fired once per combat damage step per
     *  damaged player in {@code CombatDamageService.checkGraveyardCombatDamageToYouOrPlaneswalkerTriggers}.
     *  Unlike the other graveyard slots this one supports targeting: the trigger is routed through
     *  the {@code AttackTriggerTarget} pending-choice pipeline, so the card's {@code target(...)}
     *  filter narrows the legal targets. Used by Vengeful Pharaoh. */
    GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER,
    /** Triggers whenever a land the controller controls enters the battlefield, while this card is
     *  in the controller's graveyard. Like {@link #ON_ALLY_LAND_ENTERS_BATTLEFIELD} but fired from
     *  the graveyard. Wrap the effect in {@code TriggeringCardConditionalEffect} to filter by the
     *  entering land (e.g. Reach of Branches — "whenever a Forest you control enters"). Checked in
     *  {@code TriggerCollectionService.checkAllyLandEntersTriggers}. */
    GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD,
    /** Triggers whenever a creature the controller controls enters the battlefield, while this card is
     *  in the controller's graveyard. Like {@link #ON_ALLY_CREATURE_ENTERS_BATTLEFIELD} but fired from
     *  the graveyard. Wrap the effect in {@code TriggeringCardConditionalEffect} to filter by the
     *  entering creature (e.g. Unconventional Tactics — "whenever a Zombie you control enters"). Checked
     *  in {@code TriggerCollectionService.checkAllyCreatureEntersTriggers}. */
    GRAVEYARD_ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
    /** Triggers whenever a creature the controller controls dies, while this card is in the
     *  controller's graveyard. Like {@link #ON_ALLY_CREATURE_DIES} but fired from the graveyard.
     *  Checked in {@code TriggerCollectionService.checkGraveyardAllyCreatureDeathTriggers}. */
    GRAVEYARD_ON_ALLY_CREATURE_DIES,
    /** Triggers from a graveyard when a creature enters from that graveyard or is cast from it. */
    GRAVEYARD_ON_CREATURE_ENTERS_FROM_GRAVEYARD_OR_CAST_FROM_GRAVEYARD,
    /** Triggers whenever an artifact the controller controls enters the battlefield, while this card is
     *  in the controller's graveyard. Like {@link #ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD} but fired from
     *  the graveyard. Wrap the effect in {@code TriggeringCardConditionalEffect} to filter by the
     *  entering artifact. Checked in {@code TriggerCollectionService.checkAllyArtifactEntersTriggers}. */
    GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
    /** "Whenever an opponent is dealt damage by a red instant or sorcery spell you control or by a red
     *  planeswalker you control" — fired from the controller's graveyard (Chandra's Phoenix). Checked in
     *  {@code TriggerCollectionService.checkRedSpellOrPlaneswalkerDamageToOpponentTriggers}, called from
     *  the noncombat player-damage path in {@code DamageSupport}: the resolving stack entry must be an
     *  instant/sorcery spell that is red, or an activated/triggered ability whose source card is a red
     *  planeswalker, and the damaged player must be an opponent of that entry's controller. */
    GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER,
    /** Triggers whenever an instant or sorcery spell the controller controls deals damage to an
     *  opponent or a battle, while this card is in the controller's graveyard. Checked from the
     *  batched non-combat damage flush in {@code DamageSupport}. Used by Bloodfeather Phoenix. */
    GRAVEYARD_ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_OPPONENT_OR_BATTLE,
    /** Triggers whenever one or more +1/+1 counters are put on this permanent.
     *  Fired from {@code PermanentCounterSupport} after each counter-placement event (once per
     *  event regardless of count). Used by Berta, Wise Extrapolator. */
    ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
    ON_SELF_REACHES_LEVEL_TWO,
    ON_SELF_REACHES_LEVEL_THREE,
    /** Triggers whenever one or more counters are put on this permanent. */
    ON_SELF_COUNTERS_PUT,
    /** Triggers whenever one or more +1/+1 counters are put on another non-Hydra creature the
     *  controller controls. Fired once per counter-placement event. */
    ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE,
    /** Triggers whenever one or more +1/+1 counters are put on another permanent the controller
     *  controls for the first time that turn. */
    ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_PERMANENT_FIRST_TIME_EACH_TURN,
    /** Triggers whenever one or more +1/+1 counters are put on a permanent the controller controls. */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT,
    /** Triggers whenever one or more +1/+1 counters are put on a creature the controller controls. */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE,
    /** Triggers whenever the controller puts one or more counters on a permanent or player. */
    ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER,
    /** Triggers once for each counter put on a creature the controller controls. */
    ON_ALLY_COUNTER_PUT_ON_CREATURE,
    /** Triggers whenever this permanent evolves — i.e. its evolve trigger resolves and actually puts
     *  a +1/+1 counter on it. Fired from {@code EvolveTriggerEffectHandler} only when the counter
     *  lands (no counter, no trigger). Used by Renegade Krasis. */
    ON_SELF_EVOLVES,
    /** Triggers whenever one or more loyalty counters are removed from this permanent. */
    ON_SELF_LOYALTY_COUNTERS_REMOVED,
    /** Triggers whenever one or more loyalty counters are put on planeswalkers the controller controls. */
    ON_YOU_PUT_LOYALTY_COUNTERS_ON_PLANESWALKERS,
    /** Triggers whenever the controller puts one or more -1/-1 counters on this permanent — the -1/-1
     *  mirror of {@link #ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT}, restricted to the controller's own
     *  placements ("Whenever you put one or more -1/-1 counters on this creature"). Fired once per
     *  counter-placement event regardless of how many counters were placed, only when the placing
     *  player is this permanent's controller. Fired from
     *  {@code PermanentCounterSupport.fireSelfMinusOneMinusOneCountersPutTriggers}. A targeted effect
     *  in this slot (its {@code targetSpec()} narrows the legal target) has its target chosen as the
     *  ability goes on the stack via the {@code SpellTargetTriggerAnyTarget} interaction. Used by
     *  Defiant Greatmaw ("… remove a -1/-1 counter from another target creature you control"). */
    ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT,
    /** Global watcher: triggers whenever a -1/-1 counter is put on a creature (any creature, on any
     *  battlefield, from any source — counter placement, infect/wither damage, proliferate, or a
     *  creature entering with -1/-1 counters incl. persist). Fired from
     *  {@code PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers}. Per the
     *  Gatherer ruling the ability triggers once for each individual -1/-1 counter, so the firing
     *  pushes a separate trigger per counter. Used by Flourishing Defenses. */
    ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE,
    /** Controller-restricted watcher: triggers only when the permanent's controller is the player who
     *  puts one or more -1/-1 counters on a creature (any creature, on any battlefield). Unlike the
     *  global {@link #ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE}, a counter an opponent puts (e.g.
     *  their wither/infect creature dealing damage, or their spell) does NOT fire this. The placing
     *  player is {@code gameData.currentlyResolvingControllerId} for stack-resolution placements and the
     *  damage source's / permanent's controller for combat placements. Fired per individual counter from
     *  {@code PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers}. Used by
     *  Nest of Scarabs ("Whenever you put one or more -1/-1 counters on a creature"). */
    ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE,
    /** Controller-restricted watcher that fires **once per creature per placement instance**,
     *  regardless of how many -1/-1 counters were placed on that creature at once — the "one or more
     *  counters, do it once" cadence. Contrast with {@link #ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE}
     *  (Nest of Scarabs), which fires once per individual counter to produce "that many" of something.
     *  Like that slot it triggers only when the permanent's controller is the player placing the counters.
     *  Fired from {@code PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers}.
     *  Non-targeting. Used by Hapatra, Vizier of Poisons ("Whenever you put one or more -1/-1 counters on
     *  a creature, create a 1/1 green Snake creature token with deathtouch"). */
    ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE,
    /** Triggers whenever one or more cards leave the controller's graveyard.
     *  Fires once per leave event (batched when multiple cards leave together).
     *  Checked in {@code GraveyardService.notifyCardsLeftGraveyard}. */
    ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
    /** Triggers whenever one or more cards are exiled from the controller's graveyard. */
    ON_CONTROLLER_CARDS_EXILED_FROM_GRAVEYARD,
    /** Triggers whenever one or more controlled creatures or creature cards in the controller's graveyard are exiled. */
    ON_CONTROLLER_CREATURES_OR_CREATURE_CARDS_EXILED,
    /** Triggers during your turn whenever one or more cards are exiled from graveyards and/or the battlefield. */
    ON_CARDS_EXILED_FROM_GRAVEYARDS_OR_BATTLEFIELD_DURING_YOUR_TURN,
    /** Triggers whenever this permanent's controller proliferates. */
    ON_CONTROLLER_PROLIFERATES,
    /** Triggers whenever the controller of this card proliferates while this card is in that
     *  player's graveyard. */
    GRAVEYARD_ON_CONTROLLER_PROLIFERATES,
    /** Triggers whenever one or more cards are put into exile during this permanent controller's turn. */
    ON_CONTROLLER_CARDS_EXILED_DURING_TURN,
    /** Triggers whenever one or more creature cards leave the controller's graveyard. */
    ON_CONTROLLER_CREATURE_CARDS_LEAVE_GRAVEYARD,
    /** Triggers once for each creature card that leaves the controller's graveyard. */
    ON_CONTROLLER_CREATURE_CARD_LEAVES_GRAVEYARD,
    /** Triggers whenever one or more artifact and/or creature cards leave the controller's graveyard. */
    ON_CONTROLLER_ARTIFACT_OR_CREATURE_CARDS_LEAVE_GRAVEYARD,
    /** Triggers whenever a creature controlled by the same player explores.
     *  Fired from {@code ExploreEffectHandler} (land branch) and
     *  {@code MayMiscHandlerService} (non-land branch) after explore completes. */
    ON_ALLY_CREATURE_EXPLORES,
    /** Triggers when this permanent exploits a creature (CR 702.110): its controller sacrificed
     *  a creature as its {@code ExploitEffect} ETB ability resolved, and this permanent was still
     *  on the battlefield at the start of that resolution (sacrificing itself still counts).
     *  Fired from the exploit sacrifice completion path. Used by Overcharged Amalgam. */
    ON_EXPLOIT,
    /** Triggers once per attacking creature the controller controls. Unlike ON_ALLY_CREATURES_ATTACK
     *  (which fires once per combat), this fires separately for each creature declared as an attacker.
     *  Supports TriggeringCardConditionalEffect and TriggeringPermanentConditionalEffect to filter
     *  by the attacking creature (e.g. Vampires and creatures with counters).
     *  Checked in {@code CombatAttackService.declareAttackers}. Used by Sanctum Seeker. */
    ON_ALLY_CREATURE_ATTACKS,
    /** Triggers once per unblocked attacking creature the controller controls, during the
     *  declare-blockers step (once "isn't blocked" is determined). Supports
     *  {@code TriggeringCardConditionalEffect} to filter by the unblocked creature (e.g. Rogues).
     *  The queued trigger sets the unblocked creature as the (non-targeting) {@code sourcePermanentId}
     *  so self-scoped effects like {@code BoostSelfEffect} apply to "it" (the unblocked creature),
     *  not the trigger's source permanent. Checked in {@code CombatBlockService}. Used by
     *  Stinkdrinker Bandit. */
    ON_ALLY_CREATURE_ATTACKS_UNBLOCKED,
    /** Triggers whenever a creature controlled by the same player becomes the target of a spell
     *  or ability controlled by an opponent. Fires on ALL permanents with this slot on the
     *  creature's controller's battlefield (not just the targeted creature).
     *  Checked in {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers}
     *  and {@code TriggerCollectionService.checkBecomesTargetOfAbilityTriggers}. */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
    /** Triggers whenever a creature or creature spell controlled by the same player becomes the
     *  target of a spell or ability controlled by an opponent. Fires on all permanents with this
     *  slot on that player's battlefield. Checked in {@code TriggerCollectionService}. */
    ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
    /** Triggers whenever the controller of this permanent, or any permanent they control, becomes
     *  the target of a spell or ability controlled by an opponent. Fires for all permanent types
     *  and for player targets. Checked in {@code TriggerCollectionService} from both the spell and
     *  ability target paths. */
    ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
    ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
    /** Triggers once for each other permanent controlled by this permanent's controller that
     * becomes the target of a spell or ability controlled by an opponent. The targeted permanent
     * is stored as the triggered entry's non-targeting {@code triggeringPermanentId}. Checked in
     * {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers} and
     * {@code TriggerCollectionService.checkBecomesTargetOfAbilityTriggers}. */
    ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
    /** Triggers whenever ANY creature (any controller) becomes the target of ANY spell or ability.
     *  Fires on ALL permanents with this slot across every battlefield (not just the targeted
     *  creature). The targeted creature's permanent ID is set as the non-targeting {@code targetId}
     *  on the stack entry so the resolved effect can act on it. Checked in
     *  {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers} and
     *  {@code checkBecomesTargetOfAbilityTriggers}. Used by Cowardice. */
    ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
    /** Triggers whenever a creature an opponent controls becomes the target of a spell or ability
     *  controlled by this permanent's controller. Fires on ALL permanents with this slot on the
     *  spell/ability controller's battlefield (not just the targeted creature). The targeted
     *  creature's permanent ID is set as the non-targeting {@code targetId} and the listening
     *  permanent as the {@code sourcePermanentId}, so duration-linked effects like
     *  {@code GainControlOfTargetEffect(WHILE_SOURCE_ON_BATTLEFIELD)} resolve correctly. Checked in
     *  {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers} and
     *  {@code checkBecomesTargetOfAbilityTriggers}. Used by Willbreaker. */
    ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
    /** Triggers whenever a creature controlled by the same player becomes the target of a spell.
     *  Fires on all permanents with this slot on the creature's controller's battlefield. The
     *  spell path only checks this slot; activated abilities do not trigger it. */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL,
    /** Triggers whenever a creature controlled by the same player becomes the target of a backup
     *  ability. The triggering backup ability is stored by its source card ID so the effect can
     *  copy it while it remains on the stack. */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY,
    /** Triggers whenever a creature controlled by the same player becomes the target of an instant
     *  or sorcery spell — regardless of who controls that spell. Fires on ALL permanents with this
     *  slot on the creature's controller's battlefield (not just the targeted creature). The
     *  targeted creature's permanent ID is set as the non-targeting {@code targetId} on the stack
     *  entry so the resolved effect can act on it. Spell path only (abilities never trigger it).
     *  Checked in {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers}.
     *  Used by Wild Defiance. */
    ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY,
    /** Triggers when this permanent transforms from its front face to its back face.
     *  Checked in {@code AnimationResolutionService.resolveTransformSelf} after the
     *  permanent's card reference is switched to the back face. */
    ON_TRANSFORM_TO_BACK_FACE,
    /** Triggers when this permanent transforms from its back face to its front face.
     *  Checked in {@code AnimationResolutionService.resolveTransformSelf} after the
     *  permanent's card reference is switched back to the original front face. */
    ON_TRANSFORM_TO_FRONT_FACE,
    /** Holds abilities and replacement effects that apply when a face-down permanent is turned face up. */
    ON_TURNED_FACE_UP,
    /** Fires when this creature or another creature its controller controls is turned face up. */
    ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
    /** Fires when this permanent or another permanent its controller controls is turned face up. */
    ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
    /** Triggers once per attacking creature whenever a creature attacks the controller of this
     *  permanent or a planeswalker they control. Fires on the defending player's permanents.
     *  The attacking creature's permanent ID is set as the (non-targeting) targetId on the stack
     *  entry so the resolved effect can act on it. Checked in
     *  {@code CombatAttackService.declareAttackers}. Used by Lost in the Woods. */
    ON_CREATURE_ATTACKS_YOU,
    /** Triggers once per combat when one or more creatures attack the controller of this permanent.
     *  Unlike {@link #ON_CREATURE_ATTACKS_YOU} this fires a single time no matter how many creatures
     *  attack, and only counts creatures attacking the player directly (attacking a planeswalker they
     *  control does not trigger it). Fires on the attacked player's permanents; no targetId is set, so
     *  effects scale via {@code PermanentCount(PermanentIsAttackingSourceControllerPredicate(), ...)}.
     *  Checked in {@code CombatAttackService.declareAttackers}. Used by Orim's Prayer. */
    ON_CREATURES_ATTACK_YOU,
    /** Triggers once per combat when an opponent attacks a planeswalker controlled by this
     *  permanent's controller with one or more creatures. The attacking player's ID is stored as
     *  the non-targeting {@code targetId} on the stack entry. */
    ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL,
    /** Triggers once per attacking creature whenever a creature attacks, regardless of who controls
     *  the attacker or whom it attacks. Fires on every permanent with this slot across all
     *  battlefields (e.g. Caltrops pings every attacker). The attacking creature's permanent ID is
     *  set as the (non-targeting) targetId on the stack entry so the resolved effect can act on it.
     *  Checked in {@code CombatAttackService.declareAttackers}. Used by Caltrops. */
    ON_ANY_CREATURE_ATTACKS,
    /** Triggers once per combat whenever a player attacks with one or more creatures, regardless of
     *  who controls the attackers. Fires on every permanent with this slot across all battlefields.
     *  The attacking player's ID is set as the (non-targeting) targetId on the stack entry, so
     *  player-scoped effects (e.g. {@code EachPermanentScope.TARGET_PLAYER}) act on "that player".
     *  Checked in {@code CombatAttackService.declareAttackers}. Used by Total War. */
    ON_ANY_PLAYER_ATTACKS,
    /** Triggers when this instant/sorcery spell is cast (a "when you cast this spell" ability on the
     *  spell itself). Scanned against the just-cast card in
     *  {@code TriggerCollectionService.checkSpellCastTriggers}. Used by the SOS Infusion copy cycle
     *  (e.g. Lumaret's Favor) via {@code CopyThisSpellIfConditionEffect}. */
    ON_SELF_CAST,
    /** Marker slot: "The first spell you cast each turn has cascade." Holds a {@code CascadeEffect};
     *  detected by presence (not effect type) in {@code TriggerCollectionService.checkSpellCastTriggers},
     *  which — when the casting player casts their first spell of the turn — queues that CascadeEffect as
     *  a triggered ability keyed to the just-cast spell (so the cascade threshold is the spell's mana
     *  value, not this permanent's). Used by Maelstrom Nexus. */
    GRANT_CASCADE_TO_FIRST_SPELL,
    /** Triggers whenever the controller clashes (MTG rule 701.29). Fired from
     *  {@code TriggerCollectionService.performClash} after the clash ends. Targeting triggers route
     *  through the {@code PermanentChoiceContext.ClashTriggerTarget} interaction so the controller
     *  chooses a target creature an opponent controls (Entangling Trap); non-targeting triggers go
     *  straight onto the stack as a triggered ability (Rebellion of the Flamekin). Effects wrapped in
     *  {@code IfWonClashEffect} apply only when the controller won the clash, and effects wrapped in
     *  {@code IfLostClashEffect} only when they did not win — exactly one branch fires when both are
     *  listed. */
    ON_CONTROLLER_CLASHES,
    /** Triggers whenever a player loses the game. Fired from {@code GameOutcomeService}
     *  at the moment a player is determined to lose (life/poison loss in
     *  {@code checkWinCondition}, or a direct loss via {@code declareWinner}).
     *  Note: this engine is strictly 2-player and the game ends the instant a player
     *  loses, so in practice this trigger goes onto the stack but the game finishes
     *  before it can resolve. Used by Withengar Unbound. */
    ON_PLAYER_LOSES_GAME,
    /** Triggers once when this creature blocks two or more creatures. Unlike ON_BLOCK (which fires
     *  per blocker assignment), this fires exactly once during the declare-blockers step when the
     *  creature is assigned to block 2+ attackers. The effect is resolved against the blocker itself
     *  (sourcePermanentId), so self-scoped effects like {@code GrantKeywordEffect(FIRST_STRIKE, SELF)}
     *  apply to the blocker. Checked in {@code CombatBlockService}. Used by Lairwatch Giant. */
    ON_BLOCKS_MULTIPLE_CREATURES,
    /** Global watcher slot: triggers whenever any creature blocks, whoever controls it ("Whenever a
     *  creature blocks"). Fires once per blocking creature during the declare-blockers step, even if
     *  that creature blocks several attackers, and is checked on every battlefield. The blocking
     *  creature is baked in as the trigger's (non-targeting) {@code targetId}, so
     *  {@code LoseLifeEffect(N, TARGET_PERMANENT_CONTROLLER)} hits "that creature's controller".
     *  Checked in {@code CombatBlockService}. Used by Carnage Gladiator. */
    ON_ANY_CREATURE_BLOCKS,
    /** Triggers when a creature is championed with this permanent (i.e. exiled by this permanent's
     *  Champion ability). Fired from {@code PermanentChoiceBattlefieldHandlerService.handleChampionCreature}
     *  right after the championed creature is exiled. Effects that target a player are routed through
     *  the {@code PermanentChoiceContext.ChampionedTriggerTarget} interaction. Used by Mistbind Clique
     *  ("When a Faerie is championed with this creature, tap all lands target player controls"). */
    ON_CHAMPIONED,
    /** Triggers whenever the controller of this permanent activates an activated ability (including
     *  mana abilities) of a permanent they control. Fires on every permanent with this slot on the
     *  activating player's battlefield. Wrap the effect in {@code TriggeringPermanentConditionalEffect}
     *  to filter by the permanent whose ability was activated (e.g. Ceaseless Searblades —
     *  "whenever you activate an ability of an Elemental"). Checked in
     *  {@code TriggerCollectionService.checkControllerActivatesAbilityTriggers}, driven from
     *  {@code ActivatedAbilityExecutionService.completeActivationAfterCosts}. */
    ON_CONTROLLER_ACTIVATES_ABILITY,
    /** Triggers whenever the controller activates an exhaust ability. */
    ON_CONTROLLER_ACTIVATES_EXHAUST_ABILITY,
    /** Triggers whenever this creature saddles a Mount or crews a Vehicle during its controller's
     *  main phase. The Mount or Vehicle is stored as the non-targeting source permanent on the
     *  triggered ability's stack entry. Checked from {@code CrewCostHandler} as each creature is
     *  tapped to pay the power-based cost. */
    ON_SELF_SADDLES_OR_CREWS_DURING_MAIN_PHASE,
    /** Triggers whenever the controller of this permanent activates a non-mana activated ability
     *  (CR 605.1a). Unlike {@link #ON_CONTROLLER_ACTIVATES_ABILITY} this excludes mana abilities and
     *  fires only after the ability has been put on the stack, so the triggering ability can be
     *  snapshotted and copied. Carries a {@link CopyControllerActivatedAbilityTriggerEffect}; the
     *  trigger is built in {@code TriggerCollectionService.checkControllerActivatesNonManaAbilityTriggers}.
     *  Used by Rings of Brighthearth. */
    ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
    /** Triggers whenever the controller of this permanent activates an embalm or eternalize ability
     *  (a graveyard-activated ability that creates a token copy of its source, see
     *  {@code ActivatedAbility.isEmbalmOrEternalize()}). Fires once per activation on every permanent
     *  with this slot on the activating player's battlefield, after the ability is on the stack.
     *  Checked in {@code TriggerCollectionService.checkControllerActivatesEternalizeOrEmbalmTriggers},
     *  driven from {@code AbilityActivationService.completeGraveyardAbilityActivation}. Used by
     *  Vizier of the Anointed ("whenever you activate an eternalize or embalm ability, draw a card"). */
    ON_CONTROLLER_ACTIVATES_ETERNALIZE_OR_EMBALM,
    /** Triggers whenever an <em>opponent</em> of this permanent's controller activates a non-mana
     *  activated ability (CR 605.1a) of a permanent. Fires on every permanent NOT controlled by the
     *  activating player that has this slot; like mana abilities never reach the check, the
     *  "if it isn't a mana ability" clause is automatic. Wrap the effect in
     *  {@code TriggeringPermanentConditionalEffect} to filter by the permanent whose ability was
     *  activated (e.g. Harsh Mentor — "an ability of an artifact, creature, or land"). The activating
     *  player is baked as the non-targeting {@code targetId} so a player-directed effect (e.g.
     *  {@code DealDamageToPlayersEffect(2, TARGET_PLAYER)}) acts on "that player". Checked in
     *  {@code TriggerCollectionService.checkOpponentActivatesNonManaAbilityTriggers}, driven from
     *  {@code ActivatedAbilityExecutionService.completeActivationAfterCosts}. Used by Harsh Mentor. */
    ON_OPPONENT_ACTIVATES_NONMANA_ABILITY,
    /** Triggers whenever a creature the controller controls becomes blocked. Fires once per blocked
     *  attacker, on every permanent with this slot on the blocked creature's controller's battlefield
     *  (not just the blocked creature). The blocked creature's permanent ID is set as the non-targeting
     *  {@code sourcePermanentId} on the stack entry so self-scoped effects like {@code BoostSelfEffect}
     *  apply to "it" (the blocked creature). Wrap the effect in {@code TriggeringCardConditionalEffect}
     *  to filter by the blocked creature. Checked in {@code CombatBlockService}. Used by Unstoppable Ash. */
    ON_ALLY_CREATURE_BECOMES_BLOCKED,
    /** Global watcher: triggers once for every attacker/blocker pair created in the declare-blockers
     *  step, on every permanent with this slot across all battlefields, regardless of who controls
     *  the creatures involved. Effects implementing {@code BlockPairConditionalEffect} are filtered
     *  at trigger time against the pair (e.g. "by a creature with lesser power"), and the participant
     *  the effect names is baked as the non-targeting {@code targetId} on the stack entry, with the
     *  attacker as {@code sourcePermanentId}. A {@code BlockedCreatureTriggerEffect} wrapper instead
     *  filters the blocked attacker by card predicate and fires once per matching attacker. Checked
     *  in {@code CombatBlockService}. Used by No Quarter and Spined Sliver. */
    ON_ANY_CREATURE_BECOMES_BLOCKED,
    /** Global watcher: triggers once each time blockers are declared and at least one creature
     *  blocked, regardless of who controls the blockers. Unlike
     *  {@link #ON_ANY_CREATURE_BECOMES_BLOCKED} it fires a single time no matter how many
     *  attacker/blocker pairs were created. Fires on every permanent with this slot across all
     *  battlefields; no {@code targetId} is set, so effects act on the board (e.g. an all-matching
     *  sacrifice filtered by {@code PermanentIsBlockingPredicate}). Checked in
     *  {@code CombatBlockService}. Used by Tide of War. */
    ON_ANY_CREATURES_BLOCK,
    /** Triggers whenever a permanent is returned to a player's hand (bounced from the battlefield),
     *  regardless of who controls this permanent or owns the returned one. Fires on every permanent
     *  with this slot across all battlefields, once per returned permanent. The player the permanent
     *  returned to (its owner) is set as the non-targeting {@code targetId} on the stack entry, so a
     *  player-directed effect (e.g. {@code DiscardEffect(1, TARGET_PLAYER)}) acts on "that player".
     *  Checked in {@code TriggerCollectionService.checkPermanentReturnedToHandTriggers}, driven from
     *  the single {@code PermanentRemovalService.removePermanentToHand} choke point. Used by Warped Devotion. */
    ON_ANY_PERMANENT_RETURNED_TO_HAND,
    /** Triggers whenever a creature is returned from the battlefield to this permanent's controller's hand. */
    ON_CONTROLLER_CREATURE_RETURNED_TO_HAND,
    /** Triggers whenever a permanent is returned from the battlefield to this permanent's controller's hand. */
    ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND,
    /** Global watcher: triggers whenever any source (creature or spell) deals damage, regardless of
     *  who controls it or what it damages. Holds a {@code ReflectSourceDamageToItsControllerEffect}
     *  carrying the color the watcher reacts to. Fires on every permanent with this slot across all
     *  battlefields. All damage a single source deals simultaneously is summed into one trigger
     *  (CR ruling), so it is driven from the batched damage-event boundaries: combat damage steps
     *  ({@code CombatDamageService}, per source via {@code state.combatDamageDealt}) and the end of a
     *  non-combat stack-entry resolution ({@code DamageSupport} accumulates,
     *  {@code EffectResolutionService} flushes). Queued via
     *  {@code TriggerCollectionService.queueSourceDealsDamageReflections}. Used by Justice. */
    ON_ANY_SOURCE_DEALS_DAMAGE,
    /** Triggers whenever an instant or sorcery spell its controller controls deals damage — "Whenever
     *  an instant or sorcery spell you control deals damage, ...". Fires once per damage event no
     *  matter how many objects the spell damaged simultaneously (all of it is one batched event), and
     *  the summed total is snapshotted onto the queued ability's {@code eventValue}. Shares the
     *  batched non-combat choke point that drives {@link #ON_ANY_SOURCE_DEALS_DAMAGE}
     *  ({@code DamageSupport} accumulates, {@code EffectResolutionService} flushes) via
     *  {@code TriggerCollectionService.queueSourceDealsDamageReflections}. Used by Blaze Commando. */
    ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE,
    /** Triggers whenever <em>this permanent itself</em> deals damage (combat or non-combat) to
     *  anything — a creature, a player, or a planeswalker. Unlike {@link #ON_ANY_SOURCE_DEALS_DAMAGE}
     *  (a global watcher that reacts to every source), this fires only for the damage the source keyed
     *  by the trigger dealt, so it stays with the source even when it dies dealing that damage. All
     *  damage the source deals simultaneously is summed into one trigger (CR ruling), and that summed
     *  total is snapshotted onto the queued ability's {@code eventValue} so a "that much" amount
     *  ({@code EventValue}) can read it. Shares the batched choke point that drives
     *  {@link #ON_ANY_SOURCE_DEALS_DAMAGE} ({@code CombatDamageService} per source,
     *  {@code DamageSupport} for non-combat), via
     *  {@code TriggerCollectionService.queueSourceDealsDamageReflections}. Used by El-Hajjâj
     *  ({@code GainLifeEffect(new EventValue())} — "you gain that much life"). */
    ON_SELF_DEALS_DAMAGE,
    /** Triggers whenever this permanent itself deals combat damage to anything — a creature, a
     *  player, or a planeswalker. All combat damage dealt by the source in one combat damage step
     *  is summed into one trigger. */
    ON_SELF_DEALS_COMBAT_DAMAGE,
    /** Triggers whenever this permanent itself deals combat damage to a player or planeswalker. */
    ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
    /** Triggers whenever this permanent itself deals combat damage to a player or battle. */
    ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
    /** Triggers whenever the creature equipped by this permanent deals combat damage to a player or battle. */
    ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE,
    /** Triggers whenever a creature its controller controls deals combat damage to anything — a
     *  creature, a player, or a planeswalker. Fires on the watcher permanent (which need not be a
     *  creature), once per damage-dealing creature per combat damage step; all damage that creature
     *  deals simultaneously is one trigger. The trigger is non-targeting and binds the watcher as its
     *  source permanent, so a self-referencing effect ({@code PutCountersOnSelfEffect}) acts on the
     *  watcher. Dispatched from the same per-source batched choke point as
     *  {@link #ON_SELF_DEALS_COMBAT_DAMAGE} ({@code CombatDamageService}) via
     *  {@code TriggerCollectionService.queueSourceDealsCombatDamageTriggers}. Used by Five-Alarm Fire. */
    ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE,
    /** Triggers whenever a creature the controller controls deals damage to a planeswalker. */
    ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER,
    /** Triggers whenever this permanent's controller is dealt damage (combat or non-combat, from any
     *  source — creatures, spells, abilities). Unlike {@link #ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU}
     *  (which reacts to the damage <em>source</em> and only fires for permanent sources), this fires
     *  once per damage event carrying only the amount, which is snapshotted onto the queued triggered
     *  ability's {@code eventValue} (read by an {@code EventValue} amount, e.g. "put that many
     *  counters"). Per the CR ruling, damage from multiple simultaneous sources triggers separately
     *  once for each source. Fired from the two player-damage choke points
     *  ({@code CombatDamageService} per source, {@code DamageSupport} for non-combat) via
     *  {@code TriggerCollectionService.checkControllerDealtDamageTriggers}. Used by Living Artifact. */
    ON_CONTROLLER_DEALT_DAMAGE,
    /** Triggers whenever an opponent of this permanent's controller is dealt damage. The damage
     *  amount is snapshotted onto the triggered ability's event value. */
    ON_OPPONENT_DEALT_DAMAGE,
    /** Triggers whenever a source this permanent's controller controls deals damage to a player
     *  other than them — "Whenever a source you control deals damage to another player, ...".
     *  The outbound mirror of {@link #ON_CONTROLLER_DEALT_DAMAGE}: it scans the <em>damaging</em>
     *  source's controller's battlefield, and only fires when the damaged player is someone else.
     *  Fires once per damage source, carrying the amount, which is snapshotted onto the queued
     *  triggered ability's {@code eventValue} so an {@code EventValue} amount ("put that many
     *  counters") can read it. Fired from the same two player-damage choke points as
     *  {@link #ON_CONTROLLER_DEALT_DAMAGE} ({@code CombatDamageService} per attacker,
     *  {@code DamageSupport} for non-combat) via
     *  {@code TriggerCollectionService.checkAllySourceDealtDamageToOpponentTriggers}.
     *  Used by Night Dealings. */
    ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT,
    /** Triggers whenever a source the controller controls deals noncombat damage to another player. */
    ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT,
    /** Triggers whenever this permanent's controller is dealt damage (combat or non-combat) by a
     *  source an <em>opponent</em> controls — "Whenever a source an opponent controls deals damage to
     *  you, ...". Like {@link #ON_CONTROLLER_DEALT_DAMAGE} (fires once per damage source, snapshots the
     *  amount onto the queued ability's {@code eventValue}) but gated so damage from the controller's
     *  own sources (e.g. their pain lands or self-damage spells) is ignored. The opponent gate is
     *  applied in {@code TriggerCollectionService.checkControllerDealtDamageTriggers} from the source's
     *  controller supplied by the two player-damage choke points ({@code CombatDamageService} — combat
     *  damage to the defender always comes from the active player's attackers; {@code DamageSupport} —
     *  the damaging spell/ability's controller). Used by Retaliator Griffin
     *  ({@code MayEffect(PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, new EventValue()))}). */
    ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT,
    /** Triggers whenever a source controlled by an opponent deals damage to this permanent's
     *  controller or to a permanent that controller controls. */
    ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
    /** Triggers whenever a creature deals damage (combat or non-combat) to this permanent's
     *  controller, or to a permanent they control matching the effect's filter — "Whenever a
     *  creature of the chosen color deals damage to you or a white creature you control, ...".
     *  Fires on the watcher permanent, once per damaging creature per damage event; the watcher's
     *  {@code chosenColor} gates which creatures qualify. The trigger context carries the damaging
     *  creature, the damaged permanent (null when the controller was damaged) and the amount.
     *  Fired from the four damage choke points ({@code CombatDamageService} for combat damage to
     *  the defending player and to creatures, {@code DamageSupport} for the non-combat player and
     *  creature paths) via
     *  {@code TriggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers}.
     *  Used by Mangara's Equity. */
    ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
    /** Triggers whenever a creature deals combat damage to this permanent's controller — "Whenever a
     *  creature deals combat damage to you, ...". Fires on the watcher permanent, once per damaging
     *  creature per combat damage step, from
     *  {@code TriggerCollectionService.checkDamageDealtToControllerTriggers} (combat branch only).
     *  Unlike {@link #ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU} (immediate, permanent sources of any kind,
     *  combat or not) this slot is creature- and combat-only and puts a real triggered ability on the
     *  stack whose {@code targetId} is the damaging creature, so {@code DestroyTargetPermanentEffect}
     *  resolves as "destroy that creature" without targeting. Used by Teysa, Envoy of Ghosts. */
    ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU,
    /** Triggers whenever an opponent of this card's owner gains life while this card is in
     *  that owner's graveyard. */
    GRAVEYARD_ON_OPPONENT_GAINS_LIFE,
    /** Triggers whenever this card's owner gains life while this card is in that owner's graveyard. */
    GRAVEYARD_ON_CONTROLLER_GAINS_LIFE,
    ON_OPPONENT_TOKEN_ENTERS_BATTLEFIELD,
    ON_CONTROLLER_COMMITS_CRIME,
    GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME,
    ON_SELF_BECOMES_PLOTTED,
    ON_ALLY_AURA_ATTACHED_TO_OPPONENT_NONLAND_PERMANENT,
    ON_EQUIPMENT_ATTACHED_TO_CREATURE,
    ON_SELF_BECOMES_SADDLED,
    ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE
}
