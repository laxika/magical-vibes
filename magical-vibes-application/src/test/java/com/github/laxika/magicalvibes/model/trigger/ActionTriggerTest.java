package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The player-action and object-action triggers: {@link PlayerActionTrigger} and {@link ObjectActionTrigger}. */
class ActionTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);

    @Test
    void everyActionTriggerButPlottingFunctionsFromTheBattlefieldByDefault() {
        assertThat(allFactories()).allSatisfy(trigger -> assertThat(trigger.defaultZones())
                .isEqualTo(trigger.equals(Triggers.becomesPlotted()) ? AbilityZones.EXILE : AbilityZones.BATTLEFIELD));
    }

    @Test
    void everyFactoryNamesItsOwnTrigger() {
        assertThat(allFactories()).doesNotHaveDuplicates();
    }

    @Test
    void namesTheActionAndWhoTakesIt() {
        assertThat(Triggers.youCommitCrime()).isEqualTo(new PlayerActionTrigger(
                PlayerActionTrigger.PlayerAction.COMMITS_CRIME, PlayerRelation.YOU));
        assertThat(Triggers.opponentCommitsCrime()).isEqualTo(new PlayerActionTrigger(
                PlayerActionTrigger.PlayerAction.COMMITS_CRIME, PlayerRelation.OPPONENT));
        assertThat(Triggers.playerWinsCoinFlip()).isEqualTo(new PlayerActionTrigger(
                PlayerActionTrigger.PlayerAction.WINS_COIN_FLIP, PlayerRelation.ANY));
        assertThat(Triggers.exploits()).isEqualTo(new ObjectActionTrigger(
                ObjectActionTrigger.ObjectAction.EXPLOITS, ObjectRelation.THIS));
        assertThat(Triggers.creatureYouControlExploits()).isEqualTo(new ObjectActionTrigger(
                ObjectActionTrigger.ObjectAction.EXPLOITS, ObjectRelation.YOU_CONTROL));
        assertThat(Triggers.mutates()).isNotEqualTo(Triggers.creatureYouControlMutates());
        assertThat(Triggers.ringTemptsYou()).isNotEqualTo(Triggers.ringTemptsYouAndYouChooseRingBearer());
    }

    @Test
    void needsAnActionAndWhoTakesIt() {
        assertThatThrownBy(() -> new PlayerActionTrigger(null, PlayerRelation.YOU))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PlayerActionTrigger(PlayerActionTrigger.PlayerAction.CLASHES, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ObjectActionTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ObjectActionTrigger(ObjectActionTrigger.ObjectAction.FIGHTS, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsActionAbilitiesByTriggerAndZone() {
        Card card = new Card();
        card.addTrigger(Triggers.youCommitCrime(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youCommitCrime(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                GAIN);
        card.addTrigger(Triggers.becomesPlotted(), DRAW, GAIN);

        assertThat(card.getTriggerEffects(Triggers.youCommitCrime(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youCommitCrime(), Zone.GRAVEYARD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.becomesPlotted(), Zone.EXILE)).containsExactly(DRAW, GAIN);
        assertThat(card.getTriggerEffects(Triggers.becomesPlotted(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.opponentCommitsCrime(), Zone.BATTLEFIELD)).isEmpty();
    }

    private static List<Trigger> allFactories() {
        return Stream.<Trigger>of(Triggers.youSetSchemeInMotion(), Triggers.youGiveGift(), Triggers.youSolveCase(),
                Triggers.youForetell(), Triggers.youForage(), Triggers.playersFinishVoting(),
                Triggers.ringTemptsYouAndYouChooseRingBearer(), Triggers.youInvestigateFirstTimeEachTurn(),
                Triggers.youInvestigate(), Triggers.youManifestDread(), Triggers.youCompleteDungeon(),
                Triggers.youRollOneOrMoreDice(), Triggers.youRollDie(), Triggers.youRollHighestNaturalResult(),
                Triggers.youRollNatural20(), Triggers.youBend(), Triggers.youExertCreature(),
                Triggers.youCollectEvidence(), Triggers.youDiscover(), Triggers.youSeek(), Triggers.youConjure(),
                Triggers.youBecomeMonarch(), Triggers.opponentBecomesMonarch(), Triggers.ringTemptsYou(),
                Triggers.youWinCoinFlip(), Triggers.playerWinsCoinFlip(), Triggers.youLoseCoinFlip(),
                Triggers.opponentPaysTax(), Triggers.youClash(), Triggers.playerLosesGame(), Triggers.youCommitCrime(),
                Triggers.opponentCommitsCrime(), Triggers.creatureYouControlFights(),
                Triggers.creatureYouControlExplores(), Triggers.creatureYouControlConnives(), Triggers.exploits(),
                Triggers.creatureYouControlExploits(), Triggers.creatureYouControlExploitsNontokenCreature(),
                Triggers.championsCreature(), Triggers.becomesPlotted(), Triggers.mutates(),
                Triggers.creatureYouControlMutates()).toList();
    }
}
