package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BakeIntoAPie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SugarCoat.class, BakeIntoAPie.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
class SugarCoatTest extends BaseCardTest {

    @Test
    @DisplayName("Turns an enchanted creature into a colorless Food artifact")
    void turnsCreatureIntoFoodArtifact() {
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent target = findPermanent(player2, "Llanowar Elves");

        castSugarCoat(target);

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can enchant a Food and replace its ability")
    void enchantsFood() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new BakeIntoAPie()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, food.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(gqs.isCreature(gd, food)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, food)).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature non-Food permanent")
    void rejectsNoncreatureNonFoodTarget() {
        harness.addToBattlefield(player2, new Forest());
        Permanent target = findPermanent(player2, "Forest");

        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSugarCoat(Permanent target) {
        harness.setHand(player1, List.of(new SugarCoat()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
