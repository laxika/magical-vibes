package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VonaButcherOfMaganTest extends BaseCardTest {

    // ===== Card configuration =====

    @Test
    @DisplayName("Has correct activated ability structure")
    void hasCorrectActivatedAbility() {
        VonaButcherOfMagan card = new VonaButcherOfMagan();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(PayLifeCost.class);
        PayLifeCost lifeCost = (PayLifeCost) ability.getEffects().get(0);
        assertThat(lifeCost.amount()).isEqualTo(7);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.ONLY_DURING_YOUR_TURN);
    }

    // ===== Ability: destroy target nonland permanent =====

    @Test
    @DisplayName("Destroys target nonland permanent and pays 7 life")
    void destroysNonlandPermanentAndPaysLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Taps Vona as cost")
    void tapsVonaAsCost() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());

        assertThat(vona.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate with less than 7 life")
    void cannotActivateWithInsufficientLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 6);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        assertThatThrownBy(() -> harness.activateAbility(player1, vonaIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Can activate at exactly 7 life")
    void canActivateAtExactlySevenLife() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 7);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a land permanent")
    void cannotTargetLand() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        assertThatThrownBy(() -> harness.activateAbility(player1, vonaIdx, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        // Make it player2's turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        assertThatThrownBy(() -> harness.activateAbility(player1, vonaIdx, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Can activate during combat on your turn (not sorcery speed)")
    void canActivateDuringCombatOnYourTurn() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new VonaButcherOfMagan());
        // Default: summoning sick

        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an enchantment (nonland permanent)")
    void canDestroyEnchantment() {
        Permanent vona = addCreatureReady(player1, new VonaButcherOfMagan());
        harness.setLife(player1, 20);

        // Create an enchantment-like permanent (use any non-land card)
        Card enchantment = new GrizzlyBears();
        enchantment.setName("Test Enchantment");
        Permanent enchPerm = new Permanent(enchantment);
        gd.playerBattlefields.get(player2.getId()).add(enchPerm);

        int vonaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(vona);
        harness.activateAbility(player1, vonaIdx, null, enchPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(enchPerm.getId()));
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
