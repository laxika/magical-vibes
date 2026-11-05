package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Which of the old player-action and object-action slot collectors reads a triggered ability, now that the ability
 * has a real trigger (effect-slot-refactor.md step 13, family 11, run 11a: dice, coin flips, crimes, the Ring, the
 * monarch, dungeons, voting, investigate, explore, connive, exploit, clash, mutate and the other keyword actions).
 * Each constant is named after the slot whose collector it stands for and names the trigger its cards were converted
 * to and the zone the collector reads them in. The collectors in {@code TriggerCollectionService} still decide when
 * the ability triggers, how its effects group into stack objects and which event data it is bound to, as the slot's
 * did, until step 15 of the refactor replaces them with effects that read the event.
 * <p>
 * Every collector of these watches reads the abilities themselves ({@link Trigger#collectedAsAbility()}), through
 * {@link ActionAbilityReader}: "triggers only once each turn" and the card filter of an explore, exploit or scheme
 * ability come from the ability, and the legacy view gives none of them, so {@link #effectsOf} and
 * {@link #registrationsOf} throw.
 */
public enum ActionWatch implements TriggerWatch {
    /** "Whenever you set a scheme in motion". */
    ON_CONTROLLER_SETS_SCHEME_IN_MOTION(Triggers.youSetSchemeInMotion(), Zone.BATTLEFIELD),
    /** "Whenever you give a gift". */
    ON_CONTROLLER_GIVES_GIFT(Triggers.youGiveGift(), Zone.BATTLEFIELD),
    /** "Whenever you solve a Case". */
    ON_ALLY_CASE_SOLVES(Triggers.youSolveCase(), Zone.BATTLEFIELD),
    /** "Whenever you foretell a card". */
    ON_CONTROLLER_FORETELLS(Triggers.youForetell(), Zone.BATTLEFIELD),
    /** "Whenever you forage". */
    ON_CONTROLLER_FORAGES(Triggers.youForage(), Zone.BATTLEFIELD),
    /** "Whenever players finish voting". */
    ON_PLAYERS_FINISH_VOTING(Triggers.playersFinishVoting(), Zone.BATTLEFIELD),
    /** "Whenever the Ring tempts you", read once you choose your Ring-bearer (TemptTheRingEffect). */
    ON_CONTROLLER_TEMPTS_RING(Triggers.ringTemptsYouAndYouChooseRingBearer(), Zone.BATTLEFIELD),
    /** "Whenever you investigate for the first time each turn". */
    ON_CONTROLLER_INVESTIGATES(Triggers.youInvestigateFirstTimeEachTurn(), Zone.BATTLEFIELD),
    /** "Whenever you investigate one or more times". */
    ON_CONTROLLER_INVESTIGATES_EACH_TIME(Triggers.youInvestigate(), Zone.BATTLEFIELD),
    /** "Whenever you manifest dread". */
    ON_CONTROLLER_MANIFESTS_DREAD(Triggers.youManifestDread(), Zone.BATTLEFIELD),
    /** "Whenever you complete a dungeon". */
    ON_CONTROLLER_COMPLETES_DUNGEON(Triggers.youCompleteDungeon(), Zone.BATTLEFIELD),
    /** "Whenever you complete a dungeon", while this card is in your graveyard. */
    GRAVEYARD_ON_CONTROLLER_COMPLETES_DUNGEON(Triggers.youCompleteDungeon(), Zone.GRAVEYARD),
    /** "Whenever you roll one or more dice". */
    ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE(Triggers.youRollOneOrMoreDice(), Zone.BATTLEFIELD),
    /** "Whenever you roll a die", once for each die, with its result as the event value. */
    ON_CONTROLLER_ROLLS_DIE(Triggers.youRollDie(), Zone.BATTLEFIELD),
    /** "Whenever you roll a die's highest natural result", once for each such die. */
    ON_CONTROLLER_ROLLS_HIGHEST_NATURAL_RESULT(Triggers.youRollHighestNaturalResult(), Zone.BATTLEFIELD),
    /** "Whenever you roll a natural 20", while this card is in your graveyard. */
    GRAVEYARD_ON_CONTROLLER_ROLLS_NATURAL_20(Triggers.youRollNatural20(), Zone.GRAVEYARD),
    /** "Whenever you waterbend, earthbend, firebend or airbend". */
    ON_CONTROLLER_BENDS(Triggers.youBend(), Zone.BATTLEFIELD),
    /** "Whenever you exert a creature". */
    ON_CONTROLLER_EXERTS(Triggers.youExertCreature(), Zone.BATTLEFIELD),
    /** "Whenever you collect evidence". */
    ON_CONTROLLER_COLLECTS_EVIDENCE(Triggers.youCollectEvidence(), Zone.BATTLEFIELD),
    /** "Whenever you discover". */
    ON_CONTROLLER_DISCOVERS(Triggers.youDiscover(), Zone.BATTLEFIELD),
    /** "Whenever you seek one or more cards". */
    ON_CONTROLLER_SEEKS(Triggers.youSeek(), Zone.BATTLEFIELD),
    /** "Whenever you conjure one or more cards". */
    ON_CONTROLLER_CONJURES(Triggers.youConjure(), Zone.BATTLEFIELD),
    /** "Whenever you conjure one or more cards", while this card is in your graveyard. */
    GRAVEYARD_ON_CONTROLLER_CONJURES(Triggers.youConjure(), Zone.GRAVEYARD),
    /** "Whenever you become the monarch". */
    ON_CONTROLLER_BECOMES_MONARCH(Triggers.youBecomeMonarch(), Zone.BATTLEFIELD),
    /** "Whenever an opponent becomes the monarch". */
    ON_OPPONENT_BECOMES_MONARCH(Triggers.opponentBecomesMonarch(), Zone.BATTLEFIELD),
    /** "Whenever the Ring tempts you", read as you are tempted (RingTemptsYouEffect). */
    ON_RING_TEMPTS_YOU(Triggers.ringTemptsYou(), Zone.BATTLEFIELD),
    /** "Whenever you win a coin flip". */
    ON_CONTROLLER_WINS_COIN_FLIP(Triggers.youWinCoinFlip(), Zone.BATTLEFIELD),
    /** "Whenever a player wins a coin flip". */
    ON_ANY_PLAYER_WINS_COIN_FLIP(Triggers.playerWinsCoinFlip(), Zone.BATTLEFIELD),
    /** "Whenever you lose a coin flip". */
    ON_CONTROLLER_LOSES_COIN_FLIP(Triggers.youLoseCoinFlip(), Zone.BATTLEFIELD),
    /** "Whenever an opponent pays a mana tax imposed by a spell or permanent you control". */
    ON_OPPONENT_PAYS_TAX(Triggers.opponentPaysTax(), Zone.BATTLEFIELD),
    /** "Whenever you clash". */
    ON_CONTROLLER_CLASHES(Triggers.youClash(), Zone.BATTLEFIELD),
    /** "Whenever a player loses the game". */
    ON_PLAYER_LOSES_GAME(Triggers.playerLosesGame(), Zone.BATTLEFIELD),
    /** "Whenever you commit a crime". */
    ON_CONTROLLER_COMMITS_CRIME(Triggers.youCommitCrime(), Zone.BATTLEFIELD),
    /** "Whenever an opponent commits a crime". */
    ON_OPPONENT_COMMITS_CRIME(Triggers.opponentCommitsCrime(), Zone.BATTLEFIELD),
    /** "Whenever you commit a crime", while this card is in your graveyard. */
    GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME(Triggers.youCommitCrime(), Zone.GRAVEYARD),
    /** "Whenever a creature you control fights". */
    ON_ALLY_CREATURE_FIGHTS(Triggers.creatureYouControlFights(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control explores". */
    ON_ALLY_CREATURE_EXPLORES(Triggers.creatureYouControlExplores(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control connives". */
    ON_ALLY_CREATURE_CONNIVES(Triggers.creatureYouControlConnives(), Zone.BATTLEFIELD),
    /** "When this creature exploits a creature" (CR 702.110b). */
    ON_EXPLOIT(Triggers.exploits(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control exploits a creature". */
    ON_ALLY_CREATURE_EXPLOITS(Triggers.creatureYouControlExploits(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control exploits a nontoken creature". */
    ON_ALLY_CREATURE_EXPLOITS_NONTOKEN_CREATURE(Triggers.creatureYouControlExploitsNontokenCreature(),
            Zone.BATTLEFIELD),
    /** "When a creature is championed with this creature". */
    ON_CHAMPIONED(Triggers.championsCreature(), Zone.BATTLEFIELD),
    /** "When this card becomes plotted"; it functions from exile, where a plotted card is. */
    ON_SELF_BECOMES_PLOTTED(Triggers.becomesPlotted(), Zone.EXILE),
    /** "Whenever this creature mutates". */
    ON_SELF_MUTATES(Triggers.mutates(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control mutates", the mutating creature included. */
    ON_ALLY_CREATURE_MUTATES(Triggers.creatureYouControlMutates(), Zone.BATTLEFIELD);

    private final Trigger trigger;
    private final Zone zone;

    ActionWatch(Trigger trigger, Zone zone) {
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

    /** Throws: the collectors of an action watch read its abilities themselves ({@link ActionAbilityReader}). */
    @Override
    public List<CardEffect> effectsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }

    /** Throws: the collectors of an action watch read its abilities themselves ({@link ActionAbilityReader}). */
    @Override
    public List<EffectRegistration> registrationsOf(Card card) {
        throw new UnsupportedOperationException(name() + " is read by abilities, not by effects");
    }
}
