package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValorOfTheWorthy.class, GrizzlyBears.class, FountainOfYouth.class, NevinyrralsDisk.class})
class ValorOfTheWorthyTest extends BaseCardTest {

    @Test
    @DisplayName("Valor of the Worthy gives the enchanted creature +1/+1")
    void boostsEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ValorOfTheWorthy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("When the enchanted creature leaves, Valor of the Worthy creates a flying Spirit")
    void enchantedCreatureLeavingCreatesSpirit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ValorOfTheWorthy());
        aura.setAttachedTo(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();

        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(spirits.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(spirits.getFirst().getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Valor of the Worthy cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.setHand(player1, List.of(new ValorOfTheWorthy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void returningOpponentsEnchantedCreatureCreatesSpiritForAuraController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValorOfTheWorthy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanent(player1, "Spirit").getCard().getColors()).containsExactly(CardColor.WHITE);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Valor of the Worthy");
    }

    @Test
    void exilingEnchantedCreatureCreatesSpirit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ValorOfTheWorthy());
        aura.setAttachedTo(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, bears));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        harness.assertInGraveyard(player1, "Valor of the Worthy");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void auraLeavingFirstRemovesBoostAndDoesNotCreateSpirit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ValorOfTheWorthy());
        aura.setAttachedTo(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void simultaneousDestructionOfAuraAndCreatureStillCreatesSpirit() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ValorOfTheWorthy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        aura.setAttachedTo(bears.getId());
        Permanent disk = harness.addToBattlefieldAndReturn(player1, new NevinyrralsDisk());
        disk.setSummoningSick(false);
        disk.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        harness.assertInGraveyard(player1, "Valor of the Worthy");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Nevinyrral's Disk");
    }
}
