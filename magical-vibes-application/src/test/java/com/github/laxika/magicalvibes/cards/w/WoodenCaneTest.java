package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodenCane.class, GrizzlyBears.class})
class WoodenCaneTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Wooden Cane creates a red Mutant token, attaches to it, and boosts it")
    void enteringCreatesAndAttachesMutant() {
        harness.setHand(player1, List.of(new WoodenCane()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cane = findPermanent(player1, "Wooden Cane");
        Permanent mutant = findPermanent(player1, "Mutant");
        assertThat(mutant.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(mutant.getCard().getSubtypes()).containsExactly(CardSubtype.MUTANT);
        assertThat(cane.getAttachedTo()).isEqualTo(mutant.getId());
        assertThat(gqs.getEffectivePower(gd, mutant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mutant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {3} attaches Wooden Cane to a creature you control")
    void equipAttachesToCreature() {
        Permanent cane = addReady(player1, new WoodenCane());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(cane.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
