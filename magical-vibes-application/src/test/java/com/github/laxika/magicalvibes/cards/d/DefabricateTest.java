package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FumeSpitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefabricateTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell and exiles it")
    void countersArtifactSpellAndExilesIt() {
        JayemdaeTome tome = new JayemdaeTome();
        harness.setHand(player1, List.of(tome));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, tome.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(tome.getId()));
        harness.assertNotInGraveyard(player1, "Jayemdae Tome");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot use the artifact or enchantment mode on a creature spell")
    void cannotTargetCreatureSpellWithArtifactOrEnchantmentMode() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an activated ability")
    void countersActivatedAbility() {
        FumeSpitter fumeSpitter = new FumeSpitter();
        harness.addToBattlefield(player1, fumeSpitter);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new Defabricate()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, fumeSpitter.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Fume Spitter");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
