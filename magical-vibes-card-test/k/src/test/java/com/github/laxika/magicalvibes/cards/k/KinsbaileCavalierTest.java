package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinsbaileCavalier.class, KinsbaileBorderguard.class, BenalishKnight.class})
class KinsbaileCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("Cavalier deals damage in both combat damage steps")
    void cavalierDealsDoubleStrikeCombatDamage() {
        addCreatureReady(player1, new KinsbaileCavalier());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A Knight with first strike still deals damage twice when granted double strike")
    void firstStrikeKnightDealsDoubleStrikeCombatDamage() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        addCreatureReady(player1, new BenalishKnight());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Double strike remains while another Cavalier is still on the battlefield")
    void doubleStrikeRemainsWithAnotherCavalier() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new BenalishKnight());
        Permanent knight = findPermanent(player1, "Benalish Knight");
        Permanent firstCavalier = findPermanent(player1, "Kinsbaile Cavalier");

        gd.playerBattlefields.get(player1.getId()).remove(firstCavalier);

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Kinsbaile Cavalier"), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Own Knight gains double strike")
    void ownKnightGainsDoubleStrike() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants double strike to itself (it is a Knight)")
    void grantsDoubleStrikeToItself() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Kinsbaile Cavalier"), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant double strike to own non-Knight creature")
    void doesNotGrantToNonKnight() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new KinsbaileBorderguard());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Kinsbaile Borderguard"), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to opponent's Knight")
    void doesNotGrantToOpponent() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player2, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is lost when Kinsbaile Cavalier leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent knight = findPermanent(player1, "Benalish Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kinsbaile Cavalier"));

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
