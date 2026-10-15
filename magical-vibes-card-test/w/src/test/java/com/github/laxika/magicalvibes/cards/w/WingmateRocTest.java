package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.k.KillShot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingmateRoc.class, AlpineGrizzly.class, KillShot.class})
class WingmateRocTest extends BaseCardTest {

    @Test
    @DisplayName("Raid creates a 3/4 white Bird token with flying")
    void raidCreatesBirdToken() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castWingmateRoc();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Bird");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Raid does not create a Bird token when no attack occurred")
    void noRaidDoesNotCreateBirdToken() {
        castWingmateRoc();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("Attacking gains one life for each attacking creature")
    void gainsLifeForEachAttackingCreature() {
        addCreatureReady(player1, new WingmateRoc());
        addCreatureReady(player1, new AlpineGrizzly());
        addCreatureReady(player1, new AlpineGrizzly());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        castWingmateRoc();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    void declaringNoAttackersDoesNotEnableRaid() {
        addCreatureReady(player1, new AlpineGrizzly());
        declareAttackers(List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        castWingmateRoc();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    void raidStillWorksAfterTheAttackerDies() {
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        prepareKillShot();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);

        castWingmateRoc();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(1);
    }

    @Test
    void attackingAloneCountsTheRocItself() {
        addCreatureReady(player1, new WingmateRoc());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    void otherCreaturesAttackingWithoutTheRocDoNotGainLife() {
        addCreatureReady(player1, new WingmateRoc());
        addCreatureReady(player1, new AlpineGrizzly());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void lifeGainCountsOnlyCreaturesStillAttackingAtResolution() {
        addCreatureReady(player1, new WingmateRoc());
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        addCreatureReady(player1, new AlpineGrizzly());
        prepareKillShot();
        int lifeBefore = gd.getLife(player1.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Alpine Grizzly");
        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    void removingTheRocDoesNotRemoveItsLifeGainTrigger() {
        Permanent roc = addCreatureReady(player1, new WingmateRoc());
        addCreatureReady(player1, new AlpineGrizzly());
        prepareKillShot();
        int lifeBefore = gd.getLife(player1.getId());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, roc.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Wingmate Roc");
        harness.assertLife(player1, lifeBefore + 1);
    }

    private void prepareKillShot() {
        harness.setHand(player2, List.of(new KillShot()));
        harness.addMana(player2, ManaColor.WHITE, 3);
    }

    private void castWingmateRoc() {
        harness.castFromHand(player1, new WingmateRoc(), "{3}{W}{W}");
    }
}
