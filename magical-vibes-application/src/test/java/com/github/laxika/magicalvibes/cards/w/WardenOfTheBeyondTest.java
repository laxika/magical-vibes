package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardenOfTheBeyond.class, Ornithopter.class})
class WardenOfTheBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Gets no bonus when no opponent owns a card in exile")
    void noBonusWithoutOpponentOwnedExiledCard() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent owns a card in exile")
    void getsBonusForOpponentOwnedExiledCard() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        harness.setExile(player2, List.of(new Ornithopter()));

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Does not count a card the controller owns in exile")
    void doesNotCountControllersExiledCard() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        harness.setExile(player1, List.of(new Ornithopter()));

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Loses the bonus when the opponent-owned exiled card leaves exile")
    void losesBonusWhenExiledCardLeaves() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        Card exiledCard = new Ornithopter();
        harness.setExile(player2, List.of(exiledCard));
        assertStats(4, 4);

        gd.removeFromExile(exiledCard.getId());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Multiple opponent-owned exiled cards give only one bonus, retained until the last leaves")
    void multipleExiledCardsDoNotMultiplyBonus() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        Card first = new Ornithopter();
        Card second = new Ornithopter();
        harness.setExile(player2, List.of(first, second));

        assertStats(4, 4);
        gd.removeFromExile(first.getId());
        assertStats(4, 4);
        gd.removeFromExile(second.getId());
        assertStats(2, 2);
    }

    @Test
    @DisplayName("Counts an opponent-owned card exiled face down by the controller")
    void countsFaceDownCardByOwnerRatherThanExilingPlayer() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        gd.addToExile(player2.getId(), new Ornithopter(), null, true, player1.getId());

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Does not count the controller's card exiled by the opponent")
    void doesNotCountExilingPlayerInsteadOfOwner() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        gd.addToExile(player1.getId(), new Ornithopter(), null, false, player2.getId());

        assertStats(2, 2);
    }

    @Test
    @DisplayName("Only boosts itself and checks opponents relative to each Warden's controller")
    void bonusIsSelfOnlyAndRelativeToController() {
        harness.addToBattlefield(player1, new WardenOfTheBeyond());
        Permanent opposingWarden = harness.addToBattlefieldAndReturn(player2, new WardenOfTheBeyond());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setExile(player2, List.of(new Ornithopter()));

        assertStats(4, 4);
        assertThat(gqs.getEffectivePower(gd, opposingWarden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarden)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(opposingWarden);
        gd.playerBattlefields.get(player1.getId()).add(opposingWarden);

        assertThat(gqs.getEffectivePower(gd, opposingWarden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking with vigilance does not tap Warden")
    void attackingDoesNotTap() {
        Permanent warden = addCreatureReady(player1, new WardenOfTheBeyond());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(warden.isAttacking()).isTrue();
        assertThat(warden.isTapped()).isFalse();
    }

    private void assertStats(int power, int toughness) {
        Permanent warden = findPermanent(player1, "Warden of the Beyond");
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(toughness);
    }
}
