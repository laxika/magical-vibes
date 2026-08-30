package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtraxasFall.class, AirElemental.class, AngelicChorus.class, FountainOfYouth.class,
        GrizzlyBears.class, InvasionOfInnistrad.class})
class AtraxasFallTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        destroyTarget(new FountainOfYouth(), "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys a target battle")
    void destroysBattle() {
        destroyTarget(new InvasionOfInnistrad(), "Invasion of Innistrad");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        destroyTarget(new AngelicChorus(), "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys a target creature with flying")
    void destroysCreatureWithFlying() {
        destroyTarget(new AirElemental(), "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtraxasFall()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, battle, enchantment, or creature with flying");
    }

    private void destroyTarget(Card target, String targetName) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, target);
        harness.setHand(player1, List.of(new AtraxasFall()));
        addMana();

        harness.castSorcery(player1, 0, permanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, targetName);
        harness.assertInGraveyard(player2, targetName);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
