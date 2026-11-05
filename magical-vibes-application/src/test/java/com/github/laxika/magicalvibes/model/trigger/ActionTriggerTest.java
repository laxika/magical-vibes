package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.trigger.ActionAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.ActionWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The player-action and object-action triggers: {@link PlayerActionTrigger} and {@link ObjectActionTrigger}, and how
 * the {@link ActionWatch} collectors read their abilities ({@link ActionAbilityReader}, step 14, run 16n).
 */
class ActionTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);
    private static final CardPredicate LAND = new CardTypePredicate(CardType.LAND);

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

    @Test
    void anActionAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.youCommitCrime(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.creatureYouControlExplores().withFilter(LAND), GAIN);
        card.addTrigger(Triggers.youSetSchemeInMotion(), DRAW);

        assertThat(card.getTriggeredAbilities()).allSatisfy(ability -> {
            assertThat(ability.collectedAsAbility()).isTrue();
            assertThat(ability.hasLegacyView()).isFalse();
            assertThatThrownBy(ability::legacyEffects).isInstanceOf(IllegalStateException.class);
        });
        assertThatThrownBy(() -> card.getLegacyTriggerEffects(Triggers.youCommitCrime()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ActionWatch.ON_CONTROLLER_COMMITS_CRIME.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ActionWatch.ON_ALLY_CREATURE_EXPLORES.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(card.getTriggerEffects(Triggers.youCommitCrime())).containsExactly(DRAW);
    }

    @Test
    void everyActionTriggerIsReadAsAnAbilityTheMiscActionsIncluded() {
        assertThat(ActionWatch.values()).allSatisfy(watch ->
                assertThat(watch.trigger().collectedAsAbility()).as(watch.name()).isTrue());
        assertThat(List.of(Triggers.youFullyUnlockRoom(), Triggers.crewsVehicle(),
                Triggers.creatureYouControlCrewsVehicle(), Triggers.saddlesOrCrewsDuringMainPhase(),
                Triggers.becomesCrewed(), Triggers.becomesCrewedForFirstTimeEachTurn(), Triggers.becomesSaddled(),
                Triggers.becomesSaddledForFirstTimeEachTurn(), Triggers.becomesStationed(), Triggers.doorUnlocked(),
                Triggers.doorUnlocked(1))).allSatisfy(trigger ->
                assertThat(trigger.collectedAsAbility()).as(trigger.toString()).isTrue());
    }

    @Test
    void theReaderGivesTheLimitMarkedByTheAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.youCommitCrime(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        ActionAbilityReader.ActionEffect read = ActionAbilityReader.read(
                ActionAbilityReader.printed(card, ActionWatch.ON_CONTROLLER_COMMITS_CRIME).getFirst(),
                ActionWatch.ON_CONTROLLER_COMMITS_CRIME.trigger()).getFirst();
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        assertThat(read.effect()).isSameAs(DRAW);
        assertThat(read.filter()).isNull();
        assertThat(read.triggersOnlyOnceEachTurn()).isTrue();
        assertThat(read.limitReached(gameData, sourceId)).isFalse();

        read.markLimit(gameData, sourceId);

        assertThat(read.limitReached(gameData, sourceId)).isTrue();
        assertThat(gameData.keyedOncePerTurnTriggersFiredThisTurn.get(sourceId))
                .containsExactly(read.ability().limitKey());
        assertThat(gameData.oncePerTurnTriggersFiredThisTurn).doesNotContain(sourceId);
    }

    @Test
    void theReaderGivesTheExploredExploitedOrSchemeFilterFromTheWatchedTrigger() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlExplores().withFilter(LAND), DRAW, GAIN);
        card.addTrigger(Triggers.creatureYouControlExploits().withFilter(LAND), DRAW);
        card.addTrigger(Triggers.youSetSchemeInMotion().withFilter(LAND), GAIN);

        List<ActionAbilityReader.ActionEffect> explored = ActionAbilityReader.read(
                ActionAbilityReader.printed(card, ActionWatch.ON_ALLY_CREATURE_EXPLORES),
                ActionWatch.ON_ALLY_CREATURE_EXPLORES.trigger());
        assertThat(explored).extracting(ActionAbilityReader.ActionEffect::effect).containsExactly(DRAW, GAIN);
        assertThat(explored).extracting(ActionAbilityReader.ActionEffect::filter).containsOnly(LAND);
        assertThat(ActionAbilityReader.read(ActionAbilityReader.printed(card, ActionWatch.ON_ALLY_CREATURE_EXPLOITS),
                ActionWatch.ON_ALLY_CREATURE_EXPLOITS.trigger()))
                .singleElement().extracting(ActionAbilityReader.ActionEffect::filter).isEqualTo(LAND);
        assertThat(ActionAbilityReader.read(
                ActionAbilityReader.printed(card, ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION),
                ActionWatch.ON_CONTROLLER_SETS_SCHEME_IN_MOTION.trigger()))
                .singleElement().extracting(ActionAbilityReader.ActionEffect::filter).isEqualTo(LAND);
    }

    @Test
    void theReaderWrapsTheEventConditionInsideTheInterveningIf() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.youRollDie(), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withEventCondition(new EventValueAtLeast(4)), DRAW);

        assertThat(ActionAbilityReader.read(ability, ActionWatch.ON_CONTROLLER_ROLLS_DIE.trigger()))
                .extracting(ActionAbilityReader.ActionEffect::effect)
                .containsExactly(new ConditionalEffect(new ControllerTurn(),
                        ConditionalEffect.atTriggerTime(new EventValueAtLeast(4), DRAW), true));
    }

    @Test
    void theStrictReaderRejectsAFilterOrALimitItsCollectorDoesNotJudge() {
        Card card = new Card();
        card.addTrigger(Triggers.youWinCoinFlip(), DRAW);
        card.addTrigger(Triggers.youWinCoinFlip(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), GAIN);
        Card explorer = new Card();
        explorer.addTrigger(Triggers.creatureYouControlExplores().withFilter(LAND), DRAW);

        assertThatThrownBy(() -> ActionAbilityReader.printedEffects(card, ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("judges no limit");
        assertThatThrownBy(() -> ActionAbilityReader.printedEffects(explorer, ActionWatch.ON_ALLY_CREATURE_EXPLORES))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("judges no filter");
        Card plain = new Card();
        plain.addTrigger(Triggers.youWinCoinFlip(), DRAW, GAIN);
        assertThat(ActionAbilityReader.printedEffects(plain, ActionWatch.ON_CONTROLLER_WINS_COIN_FLIP))
                .containsExactly(DRAW, GAIN);
    }

    @Test
    void whatTheActionCollectorsCannotReadIsRejected() {
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.youRollOneOrMoreDice(), TriggerOptions.ONE_OR_MORE,
                DRAW)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("player or keyword action");
        assertThat(List.of(TriggerLimit.DO_ONLY_ONCE_EACH_TURN, TriggerLimit.ONCE)).allSatisfy(limit ->
                assertThatThrownBy(() -> ActionAbilityReader.read(new TriggeredAbility(Triggers.youCommitCrime(),
                        TriggerOptions.DEFAULT.withLimit(limit), DRAW), Triggers.youCommitCrime()))
                        .as(limit.toString()).isInstanceOf(IllegalStateException.class));
        assertThatThrownBy(() -> ActionAbilityReader.read(new TriggeredAbility(Triggers.youSetSchemeInMotion(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW), Triggers.youSetSchemeInMotion()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("scheme collector");
        assertThatThrownBy(() -> ActionAbilityReader.read(new TriggeredAbility(Triggers.youClash(),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(DRAW, TriggerMode.INDEPENDENT))),
                Triggers.youClash())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> ActionAbilityReader.read(new TriggeredAbility(Triggers.youClash(), DRAW),
                Triggers.youCommitCrime())).isInstanceOf(IllegalArgumentException.class);
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
