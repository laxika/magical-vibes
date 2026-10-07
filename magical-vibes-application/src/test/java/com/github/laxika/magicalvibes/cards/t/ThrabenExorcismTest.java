package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BorrowedTime;
import com.github.laxika.magicalvibes.cards.s.SearchPartyCaptain;
import com.github.laxika.magicalvibes.cards.l.LunarchVeteran;
import com.github.laxika.magicalvibes.cards.s.SpectralAdversary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrabenExorcism.class, BorrowedTime.class, SearchPartyCaptain.class, LunarchVeteran.class, SpectralAdversary.class})
class ThrabenExorcismTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a Spirit")
    void exilesSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpectralAdversary());

        castExorcism(target);

        assertExiled(player2, "Spectral Adversary");
    }

    @Test
    @DisplayName("Exiles a creature with disturb")
    void exilesCreatureWithDisturb() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LunarchVeteran());

        castExorcism(target);

        assertExiled(player2, "Lunarch Veteran");
    }

    @Test
    @DisplayName("Exiles an enchantment")
    void exilesEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorrowedTime());

        castExorcism(target);

        assertExiled(player2, "Borrowed Time");
    }

    @Test
    @DisplayName("Rejects a creature that is neither a Spirit nor has disturb")
    void rejectsOrdinaryCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SearchPartyCaptain());
        harness.setHand(player1, List.of(new ThrabenExorcism()));
        addExorcismMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Spirit, a creature with disturb, or an enchantment");
    }

    @Test
    @DisplayName("Can exile a Spirit controlled by the caster")
    void exilesOwnSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpectralAdversary());

        castExorcism(target);

        assertExiled(player1, "Spectral Adversary");
    }

    @Test
    @DisplayName("Exiles only the chosen permanent when all target categories are present")
    void exilesOnlyChosenPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpectralAdversary());
        harness.addToBattlefield(player2, new LunarchVeteran());
        harness.addToBattlefield(player2, new BorrowedTime());

        castExorcism(target);

        assertExiled(player2, "Spectral Adversary");
        harness.assertOnBattlefield(player2, "Lunarch Veteran");
        harness.assertOnBattlefield(player2, "Borrowed Time");
    }

    @Test
    @DisplayName("Does not exile another permanent when the target has already left")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpectralAdversary());
        harness.addToBattlefield(player2, new BorrowedTime());
        harness.setHand(player1, List.of(new ThrabenExorcism(), new ThrabenExorcism()));
        addExorcismMana();
        addExorcismMana();
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertExiled(player2, "Spectral Adversary");
        harness.assertOnBattlefield(player2, "Borrowed Time");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Thraben Exorcism"))
                .hasSize(2);
    }

    private void castExorcism(Permanent target) {
        harness.setHand(player1, List.of(new ThrabenExorcism()));
        addExorcismMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addExorcismMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void assertExiled(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        harness.assertNotOnBattlefield(player, cardName);
        harness.assertNotInGraveyard(player, cardName);
        assertThat(gd.getPlayerExiledCards(player.getId()))
                .anyMatch(card -> card.getName().equals(cardName));
    }
}
