package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GolgariSignet;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FistsOfIronwood.class, GrayscaledGharial.class, GolgariSignet.class, LastGasp.class})
class FistsOfIronwoodTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates two Saprolings and grants trample to the enchanted creature")
    void enteringCreatesSaprolingsAndGrantsTrample() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());

        harness.setHand(player1, List.of(new FistsOfIronwood()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        List<Permanent> saprolings = findPermanents(player1, "Saproling");
        assertThat(saprolings).hasSize(2);
        assertThat(saprolings).allSatisfy(saproling -> {
            assertThat(saproling.getCard().isToken()).isTrue();
            assertThat(saproling.getCard().getPower()).isEqualTo(1);
            assertThat(saproling.getCard().getToughness()).isEqualTo(1);
            assertThat(saproling.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(saproling.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        });
    }

    @Test
    @DisplayName("The Saprolings are created under the Aura controller's control")
    void enteringCreatesTokensForAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrayscaledGharial());

        harness.setHand(player1, List.of(new FistsOfIronwood()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("The enters-the-battlefield ability resolves even if the Aura leaves first")
    void enteringAbilityResolvesAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());

        harness.setHand(player1, List.of(new FistsOfIronwood()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fists of Ironwood");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The trample bonus ends when Fists of Ironwood leaves the battlefield")
    void trampleEndsWhenAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());

        harness.setHand(player1, List.of(new FistsOfIronwood()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fists of Ironwood");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An Aura whose target dies before resolution creates no Saprolings")
    void targetDiesBeforeAuraResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.setHand(player1, List.of(new FistsOfIronwood(), new LastGasp()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fists of Ironwood");
        harness.assertNotOnBattlefield(player1, "Fists of Ironwood");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Saproling trigger survives the enchanted creature dying")
    void targetDiesAfterAuraEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrayscaledGharial());
        harness.setHand(player1, List.of(new FistsOfIronwood(), new LastGasp()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Grayscaled Gharial");
        harness.assertInGraveyard(player1, "Fists of Ironwood");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fists of Ironwood cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new GolgariSignet());

        harness.setHand(player1, List.of(new FistsOfIronwood()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
