package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurnAtTheStakeTest extends BaseCardTest {

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, amount);
    }

    @Test
    @DisplayName("Deals three times the number of creatures tapped to the target player")
    void dealsThreeTimesTappedCreaturesToPlayer() {
        Permanent first = new Permanent(new GrizzlyBears());
        Permanent second = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(first, second));

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Kills a target creature when enough creatures are tapped")
    void killsTargetCreature() {
        Permanent tapped = new Permanent(new GrizzlyBears());
        Permanent victim = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(tapped);
        gd.playerBattlefields.get(player2.getId()).add(victim);

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, victim.getId(), List.of(tapped.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tapping no creatures deals no damage")
    void tappingNoCreaturesDealsNoDamage() {
        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot tap an already tapped creature to pay the cost")
    void cannotTapAlreadyTappedCreature() {
        Permanent alreadyTapped = new Permanent(new GrizzlyBears());
        alreadyTapped.tap();
        gd.playerBattlefields.get(player1.getId()).add(alreadyTapped);

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(alreadyTapped.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot tap a creature an opponent controls to pay the cost")
    void cannotTapOpponentCreature() {
        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot tap a non-creature permanent to pay the cost")
    void cannotTapNonCreature() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
    }
}
