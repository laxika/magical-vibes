package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RamosianSergeant;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrutalSuppressionTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken Rebel abilities require sacrificing a land")
    void taxesNontokenRebelAbility() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new RamosianSergeant());
        Permanent land = addLand(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The added land cost can be chosen when multiple lands are available")
    void promptsForLandChoice() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new RamosianSergeant());
        Permanent firstLand = addLand(player2);
        Permanent secondLand = addLand(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());

        harness.handlePermanentChosen(player2, secondLand.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstLand).doesNotContain(secondLand);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A nontoken Rebel ability cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new RamosianSergeant());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Rebel abilities are not taxed")
    void doesNotTaxNonRebelAbility() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Token Rebels are not taxed")
    void doesNotTaxTokenRebel() {
        harness.addToBattlefield(player1, new BrutalSuppression());
        Card tokenRebel = new ZuranSpellcaster();
        tokenRebel.setToken(true);
        tokenRebel.setSubtypes(List.of(CardSubtype.REBEL));
        addCreatureReady(player2, tokenRebel);

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addLand(com.github.laxika.magicalvibes.model.Player player) {
        Permanent land = new Permanent(new Forest());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
