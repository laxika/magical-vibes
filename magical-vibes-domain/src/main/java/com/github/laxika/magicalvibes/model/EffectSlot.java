package com.github.laxika.magicalvibes.model;

public enum EffectSlot {
    ON_TAP,
    ON_ENTER_BATTLEFIELD,
    SPELL,
ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
    /** "Whenever a nontoken creature enters under your control" (excludes this permanent and tokens).
     *  Like {@link #ON_ALLY_CREATURE_ENTERS_BATTLEFIELD} but the entering permanent's id is preserved on
     *  any queued may-pay ability (mirrors {@link #ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD}), so a
     *  "you may pay {N}. If you do, create a token that's a copy of that creature" effect
     *  ({@code CreateTokenCopyOfTargetPermanentEffect}) knows which creature to copy. Checked in
     *  {@code TriggerCollectionService.checkAllyNontokenCreatureEntersTriggers}. Used by Minion Reflector. */
    ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
    ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
    ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD,
    ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD,
    STATIC,
    ON_SACRIFICE,
    ON_BLOCK,
    UPKEEP_TRIGGERED,
    GRAVEYARD_UPKEEP_TRIGGERED,
    EACH_UPKEEP_TRIGGERED,
    OPPONENT_UPKEEP_TRIGGERED,
    ON_ANY_PLAYER_CASTS_SPELL,
    ON_CONTROLLER_CASTS_SPELL,
    ON_OPPONENT_CASTS_SPELL,
    ON_DEATH,
    ON_ALLY_CREATURE_DIES,
    ON_DAMAGED_CREATURE_DIES,
    ON_COMBAT_DAMAGE_TO_PLAYER,
    ON_COMBAT_DAMAGE_TO_CREATURE,
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
     *  may-ability to replace the draw. Used by Island Sanctuary (holds an {@code IslandSanctuaryEffect}). */
    MAY_SKIP_DRAW_STEP_DRAW,
    END_STEP_TRIGGERED,
    CONTROLLER_END_STEP_TRIGGERED,
    ON_CONTROLLER_DRAWS,
    ON_OPPONENT_DRAWS,
    ON_OPPONENT_DISCARDS,
    /** Whenever the controller discards a card ("whenever you discard a card"). Fires on the discarding
     *  player's own battlefield in {@code TriggerCollectionService.checkDiscardTriggers}. Used by Necropotence. */
    ON_CONTROLLER_DISCARDS,
    ON_SELF_DISCARDED_BY_OPPONENT,
    ON_ANY_PLAYER_TAPS_LAND,
    ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
    ON_ALLY_PERMANENT_SACRIFICED,
    ON_BECOMES_TARGET_OF_SPELL,
    ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
    ON_ANY_CREATURE_DIES,
    ON_ALLY_NONTOKEN_CREATURE_DIES,
    ON_ANY_NONTOKEN_CREATURE_DIES,
    ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers whenever a permanent (of any type) an opponent of the controller controls is put into
     *  a graveyard from the battlefield. Fires on permanents controlled by an opponent of the dying
     *  permanent's controller. Checked in {@code PermanentRemovalService.processGraveyardAndTriggers}
     *  via {@code TriggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers}. Used by
     *  Prince of Thralls. */
    ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
    /** Triggers when a land the controller owns is put into their graveyard from the battlefield
     *  because of a spell or ability an opponent controls (Sacred Ground). Fires only on permanents
     *  the graveyard owner controls. */
    ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT,
    /** Triggers whenever a land card the controller owns is put into their graveyard from anywhere
     *  (battlefield, hand, library, stack, exile). Fires on permanents the graveyard owner controls.
     *  Checked in {@code GraveyardService.addCardToGraveyard} (the single zone→graveyard choke point)
     *  via {@code TriggerCollectionService.checkLandPutIntoGraveyardFromAnywhereTriggers}. Used by
     *  Countryside Crusher. */
    ON_ALLY_LAND_PUT_INTO_GRAVEYARD_FROM_ANYWHERE,
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
    ON_ENCHANTED_PERMANENT_TAPPED,
    /** Triggers whenever a permanent the controller controls becomes tapped. Fires on every
     *  permanent with this slot on the tapped permanent's controller's battlefield. Wrap the
     *  effect in {@code TriggeringPermanentConditionalEffect} to filter by the tapped permanent
     *  (e.g. Judge of Currents — "whenever a Merfolk you control becomes tapped"). Checked in
     *  {@code TriggerCollectionService.checkEnchantedPermanentTapTriggers}, driven by the same
     *  tap-event call sites as {@code ON_ENCHANTED_PERMANENT_TAPPED}. */
    ON_ALLY_PERMANENT_BECOMES_TAPPED,
    /** Triggers whenever this permanent becomes untapped (transitions from tapped to untapped),
     *  from any source — the untap step, or an untap effect. Fires only on the permanent that
     *  became untapped. Driven from the untap call sites via
     *  {@code TriggerCollectionService.checkBecomesUntappedTriggers}
     *  ({@code UntapStepService} and {@code TapUntapSupport.untapPermanent}). The trigger is queued
     *  as a non-targeting triggered ability whose {@code sourcePermanentId} is the untapped
     *  permanent; targeted "may" effects (e.g. Hollowsage's "you may have target player discard a
     *  card") pick their target at resolution via the {@code MayEffect} pending-may-ability flow. */
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
    ON_EQUIPPED_CREATURE_DIES,
    ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
    ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
    /** Triggers whenever a land the controller controls enters the battlefield.
     *  Checked in {@code BattlefieldEntryService.checkAllyLandEntersTriggers}. */
    ON_ALLY_LAND_ENTERS_BATTLEFIELD,
    ON_OPPONENT_CREATURE_DIES,
    ON_DEALT_DAMAGE,
    ON_OPENING_HAND_REVEAL,
    ON_OPPONENT_LOSES_LIFE,
    ON_OPPONENT_SHUFFLES_LIBRARY,
    ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
    /** Triggers during the end step of the enchanted permanent's controller ("At the beginning of
     *  your end step" on an ability granted to the enchanted permanent). Checked in
     *  {@code StepTriggerService.handleEndStepTriggers}. Used by Nettlevine Blight. */
    ENCHANTED_PERMANENT_CONTROLLER_END_STEP_TRIGGERED,
    ENCHANTED_PLAYER_UPKEEP_TRIGGERED,
    ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD,
    ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
    /** Triggers whenever this creature or another creature enters the battlefield from the
     *  controller's graveyard. Checked in {@code BattlefieldEntryService.checkEntersFromGraveyardTriggers}
     *  after a creature enters, using the {@code enteredFromGraveyardOwnerId} flag on the entering
     *  permanent. Routed into the any-target pipeline ({@code EntersFromGraveyardTriggerTarget} interactions).
     *  Used by Flayer of the Hatebound. */
    ON_CREATURE_ENTERS_FROM_GRAVEYARD,
    /** "Whenever this creature or another permanent enters from a graveyard" — fires for ANY permanent
     *  (not just creatures) entering the battlefield from ANY graveyard, checked via the
     *  {@code enteredFromGraveyardOwnerId} flag. Queues the resolved effects as a non-targeting stack
     *  entry for the source's controller. Used by River Kelpie. */
    ON_PERMANENT_ENTERS_FROM_GRAVEYARD,
    ON_CONTROLLER_GAINS_LIFE,
    ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE,
    ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
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
    /** State-triggered abilities (MTG rule 603.8). Checked after SBAs; fire once onto the
     *  stack and don't retrigger while the ability is already on the stack. */
    STATE_TRIGGERED,
    /** Saga chapter I ability (MTG rule 714). Triggers when the first lore counter is placed. */
    SAGA_CHAPTER_I,
    /** Saga chapter II ability (MTG rule 714). Triggers when the second lore counter is placed. */
    SAGA_CHAPTER_II,
    /** Saga chapter III ability (MTG rule 714). Triggers when the third lore counter is placed. */
    SAGA_CHAPTER_III,
    /** Triggers at the beginning of combat on the controller's turn.
     *  Checked in {@code StepTriggerService.handleBeginningOfCombatTriggers}. */
    BEGINNING_OF_COMBAT_TRIGGERED,
    /** Triggers at the beginning of the active player's precombat main phase on the
     *  controller's turn. Checked in {@code StepTriggerService.handlePrecombatMainTriggers}. */
    PRECOMBAT_MAIN_TRIGGERED,
    /** Triggers whenever a creature an opponent controls is dealt damage (combat or non-combat).
     *  Fires on the permanent with this slot, not on the damaged creature. Scans all battlefields
     *  for permanents with this slot whose controller is different from the damaged creature's controller. */
    ON_OPPONENT_CREATURE_DEALT_DAMAGE,
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
    /** Triggers whenever the controller of this permanent loses life (damage or direct life loss).
     *  Fires on the controller's own permanents. The amount is passed via TriggerContext.LifeLoss.
     *  Hooked into TriggerCollectionService.checkLifeLossTriggers(). Used by Lich's Mastery. */
    ON_CONTROLLER_LOSES_LIFE,
    /** Triggers when this permanent leaves the battlefield by any means (destruction, exile,
     *  bounce, sacrifice, tuck). Checked in PermanentRemovalService after removal. */
    ON_SELF_LEAVES_BATTLEFIELD,
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
    /** Triggers whenever a land the controller controls enters the battlefield, while this card is
     *  in the controller's graveyard. Like {@link #ON_ALLY_LAND_ENTERS_BATTLEFIELD} but fired from
     *  the graveyard. Wrap the effect in {@code TriggeringCardConditionalEffect} to filter by the
     *  entering land (e.g. Reach of Branches — "whenever a Forest you control enters"). Checked in
     *  {@code TriggerCollectionService.checkAllyLandEntersTriggers}. */
    GRAVEYARD_ON_ALLY_LAND_ENTERS_BATTLEFIELD,
    /** Triggers whenever one or more +1/+1 counters are put on this permanent.
     *  Fired from {@code PermanentCounterSupport} after each counter-placement event (once per
     *  event regardless of count). Used by Berta, Wise Extrapolator. */
    ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
    /** Global watcher: triggers whenever a -1/-1 counter is put on a creature (any creature, on any
     *  battlefield, from any source — counter placement, infect/wither damage, proliferate, or a
     *  creature entering with -1/-1 counters incl. persist). Fired from
     *  {@code PermanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers}. Per the
     *  Gatherer ruling the ability triggers once for each individual -1/-1 counter, so the firing
     *  pushes a separate trigger per counter. Used by Flourishing Defenses. */
    ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE,
    /** Triggers whenever one or more cards leave the controller's graveyard.
     *  Fires once per leave event (batched when multiple cards leave together).
     *  Checked in {@code GraveyardService.notifyCardsLeftGraveyard}. */
    ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
    /** Triggers whenever a creature controlled by the same player explores.
     *  Fired from {@code ExploreEffectHandler} (land branch) and
     *  {@code MayMiscHandlerService} (non-land branch) after explore completes. */
    ON_ALLY_CREATURE_EXPLORES,
    /** Triggers once per attacking creature the controller controls. Unlike ON_ALLY_CREATURES_ATTACK
     *  (which fires once per combat), this fires separately for each creature declared as an attacker.
     *  Supports TriggeringCardConditionalEffect to filter by the attacking creature (e.g. Vampires).
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
    /** Triggers whenever ANY creature (any controller) becomes the target of ANY spell or ability.
     *  Fires on ALL permanents with this slot across every battlefield (not just the targeted
     *  creature). The targeted creature's permanent ID is set as the non-targeting {@code targetId}
     *  on the stack entry so the resolved effect can act on it. Checked in
     *  {@code TriggerCollectionService.checkBecomesTargetOfSpellTriggers} and
     *  {@code checkBecomesTargetOfAbilityTriggers}. Used by Cowardice. */
    ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
    /** Triggers when this permanent transforms from its front face to its back face.
     *  Checked in {@code AnimationResolutionService.resolveTransformSelf} after the
     *  permanent's card reference is switched to the back face. */
    ON_TRANSFORM_TO_BACK_FACE,
    /** Triggers when this permanent transforms from its back face to its front face.
     *  Checked in {@code AnimationResolutionService.resolveTransformSelf} after the
     *  permanent's card reference is switched back to the original front face. */
    ON_TRANSFORM_TO_FRONT_FACE,
    /** Triggers once per attacking creature whenever a creature attacks the controller of this
     *  permanent or a planeswalker they control. Fires on the defending player's permanents.
     *  The attacking creature's permanent ID is set as the (non-targeting) targetId on the stack
     *  entry so the resolved effect can act on it. Checked in
     *  {@code CombatAttackService.declareAttackers}. Used by Lost in the Woods. */
    ON_CREATURE_ATTACKS_YOU,
    /** Triggers once per attacking creature whenever a creature attacks, regardless of who controls
     *  the attacker or whom it attacks. Fires on every permanent with this slot across all
     *  battlefields (e.g. Caltrops pings every attacker). The attacking creature's permanent ID is
     *  set as the (non-targeting) targetId on the stack entry so the resolved effect can act on it.
     *  Checked in {@code CombatAttackService.declareAttackers}. Used by Caltrops. */
    ON_ANY_CREATURE_ATTACKS,
    /** Triggers when this instant/sorcery spell is cast (a "when you cast this spell" ability on the
     *  spell itself). Scanned against the just-cast card in
     *  {@code TriggerCollectionService.checkSpellCastTriggers}. Used by the SOS Infusion copy cycle
     *  (e.g. Lumaret's Favor) via {@code CopyThisSpellIfConditionEffect}. */
    ON_SELF_CAST,
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
    /** Triggers whenever the controller of this permanent activates a non-mana activated ability
     *  (CR 605.1a). Unlike {@link #ON_CONTROLLER_ACTIVATES_ABILITY} this excludes mana abilities and
     *  fires only after the ability has been put on the stack, so the triggering ability can be
     *  snapshotted and copied. Carries a {@link CopyControllerActivatedAbilityTriggerEffect}; the
     *  trigger is built in {@code TriggerCollectionService.checkControllerActivatesNonManaAbilityTriggers}.
     *  Used by Rings of Brighthearth. */
    ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
    /** Triggers whenever a creature the controller controls becomes blocked. Fires once per blocked
     *  attacker, on every permanent with this slot on the blocked creature's controller's battlefield
     *  (not just the blocked creature). The blocked creature's permanent ID is set as the non-targeting
     *  {@code sourcePermanentId} on the stack entry so self-scoped effects like {@code BoostSelfEffect}
     *  apply to "it" (the blocked creature). Wrap the effect in {@code TriggeringCardConditionalEffect}
     *  to filter by the blocked creature. Checked in {@code CombatBlockService}. Used by Unstoppable Ash. */
    ON_ALLY_CREATURE_BECOMES_BLOCKED,
    /** Triggers whenever a permanent is returned to a player's hand (bounced from the battlefield),
     *  regardless of who controls this permanent or owns the returned one. Fires on every permanent
     *  with this slot across all battlefields, once per returned permanent. The player the permanent
     *  returned to (its owner) is set as the non-targeting {@code targetId} on the stack entry, so a
     *  player-directed effect (e.g. {@code DiscardEffect(1, TARGET_PLAYER)}) acts on "that player".
     *  Checked in {@code TriggerCollectionService.checkPermanentReturnedToHandTriggers}, driven from
     *  the single {@code PermanentRemovalService.removePermanentToHand} choke point. Used by Warped Devotion. */
    ON_ANY_PERMANENT_RETURNED_TO_HAND,
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
    /** Triggers whenever this permanent's controller is dealt damage (combat or non-combat, from any
     *  source — creatures, spells, abilities). Unlike {@link #ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU}
     *  (which reacts to the damage <em>source</em> and only fires for permanent sources), this fires
     *  once per damage event carrying only the amount, which is snapshotted onto the queued triggered
     *  ability's {@code eventValue} (read by an {@code EventValue} amount, e.g. "put that many
     *  counters"). Per the CR ruling, damage from multiple simultaneous sources triggers separately
     *  once for each source. Fired from the two player-damage choke points
     *  ({@code CombatDamageService} per source, {@code DamageSupport} for non-combat) via
     *  {@code TriggerCollectionService.checkControllerDealtDamageTriggers}. Used by Living Artifact. */
    ON_CONTROLLER_DEALT_DAMAGE
}
