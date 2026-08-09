package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IlluminatedWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Illuminated Wings attaches it and grants flying")
    void resolvingAttachesAndGrantsFlying() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new IlluminatedWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Illuminated Wings")
                        && p.isAttached()
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Illuminated Wings draws a card")
    void sacrificingDrawsACard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent wings = new Permanent(new IlluminatedWings());
        wings.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(wings);

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Illuminated Wings");
        harness.assertInGraveyard(player1, "Illuminated Wings");
    }

    @Test
    @DisplayName("Illuminated Wings cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new IlluminatedWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}
