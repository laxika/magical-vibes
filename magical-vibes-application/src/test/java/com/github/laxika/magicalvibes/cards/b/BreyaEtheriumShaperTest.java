package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreyaEtheriumShaper.class, Memnite.class, GrizzlyBears.class})
class BreyaEtheriumShaperTest extends BaseCardTest {

    @Test
    void entersWithTwoBlueFlyingThopterArtifacts() {
        harness.setHand(player1, List.of(new BreyaEtheriumShaper()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(thopter.getCard().getPower()).isEqualTo(1);
            assertThat(thopter.getCard().getToughness()).isEqualTo(1);
            assertThat(thopter.getCard().getColors()).containsExactly(com.github.laxika.magicalvibes.model.CardColor.BLUE);
            assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        });
    }

    @Test
    void damageModeSacrificesTwoArtifacts() {
        Permanent breya = prepareBreya();
        harness.setLife(player2, 20);

        activate(breya, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Breya, Etherium Shaper");
        assertThat(findPermanents(player1, "Memnite")).isEmpty();
    }

    @Test
    void creatureModeGivesMinusFourMinusFourUntilEndOfTurn() {
        Permanent breya = prepareBreya();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activate(breya, 1, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void lifeModeGainsFiveLife() {
        Permanent breya = prepareBreya();
        harness.setLife(player1, 20);

        activate(breya, 2, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    void creatureModeCannotTargetAPlayer() {
        Permanent breya = prepareBreya();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(breya), 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    private Permanent prepareBreya() {
        Permanent breya = harness.addToBattlefieldAndReturn(player1, new BreyaEtheriumShaper());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        return breya;
    }

    private void activate(Permanent breya, int mode, java.util.UUID targetId) {
        List<Permanent> artifacts = findPermanents(player1, "Memnite");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(breya), mode, null, targetId);
        harness.handlePermanentChosen(player1, artifacts.get(0).getId());
        harness.handlePermanentChosen(player1, artifacts.get(1).getId());
        harness.passBothPriorities();
    }
}
