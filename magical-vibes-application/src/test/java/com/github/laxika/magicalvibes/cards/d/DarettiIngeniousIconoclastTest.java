package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarettiIngeniousIconoclast.class, GrizzlyBears.class, Spellbook.class})
class DarettiIngeniousIconoclastTest extends BaseCardTest {

    @Test
    void plusOneCreatesDefenderConstruct() {
        Permanent daretti = addReadyDaretti(6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(construct.getCard().isToken()).isTrue();
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(construct.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(construct.getCard().getPower()).isEqualTo(1);
        assertThat(construct.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, construct, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void minusOneSacrificesArtifactThenDestroysTarget() {
        Permanent daretti = addReadyDaretti(6);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void minusOneMayBeDeclined() {
        Permanent daretti = addReadyDaretti(6);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(daretti.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    void ultimateCopiesBattlefieldArtifactThreeTimes() {
        addReadyDaretti(6);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Spellbook")).isEqualTo(3);
    }

    @Test
    void ultimateCopiesArtifactFromAnyGraveyardThreeTimes() {
        addReadyDaretti(6);
        Spellbook target = new Spellbook();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(countTokenCopies(player1, "Spellbook")).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
    }

    private Permanent addReadyDaretti(int loyalty) {
        Permanent daretti = harness.addToBattlefieldAndReturn(player1, new DarettiIngeniousIconoclast());
        daretti.setCounterCount(CounterType.LOYALTY, loyalty);
        daretti.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return daretti;
    }

    private long countTokenCopies(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .count();
    }
}
