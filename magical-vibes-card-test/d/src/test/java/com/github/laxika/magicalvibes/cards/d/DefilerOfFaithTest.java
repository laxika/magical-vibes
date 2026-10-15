package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenalishSleeper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrayerOfBinding;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.s.StallForTime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Defiler of Faith")
@CardUsed({DefilerOfFaith.class, SavannahLions.class, GrizzlyBears.class,
        BenalishSleeper.class, PrayerOfBinding.class, StallForTime.class})
class DefilerOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a white permanent spell by {W} and creates a Soldier")
    void paysLifeForWhitePermanentSpell() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.setLife(player1, 20);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(soldierTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("paying the reduced white mana cost leaves life unchanged and creates a Soldier")
    void paysReducedManaForWhitePermanentSpell() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(soldierTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("nonwhite permanent spells do not receive the cost reduction or trigger")
    void ignoresNonwhitePermanentSpell() {
        addCreatureReady(player1, new DefilerOfFaith());
        GrizzlyBears greenCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(greenCreature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("paying life reduces only white mana, leaving the generic cost payable")
    void payingLifeLeavesGenericCost() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new BenalishSleeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertOnBattlefield(player1, "Benalish Sleeper");
        assertThat(soldierTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("life payment cannot replace the generic mana cost")
    void lifeCannotReplaceGenericMana() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new BenalishSleeper()));

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("a player with less than 2 life cannot choose the additional life payment")
    void insufficientLifeCannotPayAdditionalCost() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new SavannahLions()));
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("the Soldier trigger resolves before a white enchantment spell")
    void whiteEnchantmentTriggersBeforeResolving() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new PrayerOfBinding()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(soldierTokens(player1)).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof PrayerOfBinding);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("white instants neither receive a reduction nor create a Soldier")
    void ignoresWhiteInstant() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new StallForTime()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("an opponent's white permanent does not trigger the Defiler")
    void ignoresOpponentWhitePermanent() {
        addCreatureReady(player1, new DefilerOfFaith());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BenalishSleeper()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(soldierTokens(player1)).isEmpty();
        assertThat(soldierTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("a Defiler on the stack does not trigger for its own casting")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new DefilerOfFaith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Defiler of Faith");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(soldierTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("two Defilers give no discount when both life payments are declined")
    void multipleDefilersWithoutLifePaymentRequireFullMana() {
        addCreatureReady(player1, new DefilerOfFaith());
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new DefilerOfFaith()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(soldierTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("two Defilers cannot remove two white mana symbols for only 2 life")
    void multipleDefilersRequireSeparateLifePayments() {
        addCreatureReady(player1, new DefilerOfFaith());
        addCreatureReady(player1, new DefilerOfFaith());
        harness.setHand(player1, List.of(new DefilerOfFaith()));
        harness.setLife(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        assertThat(soldierTokens(player1)).isEmpty();
    }

    private List<com.github.laxika.magicalvibes.model.Permanent> soldierTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> "Soldier".equals(permanent.getCard().getName()))
                .toList();
    }
}
