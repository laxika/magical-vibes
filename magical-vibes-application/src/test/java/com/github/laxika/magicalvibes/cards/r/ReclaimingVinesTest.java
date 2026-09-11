package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReclaimingVines.class, FountainOfYouth.class, AngelicChorus.class, Forest.class, GrizzlyBears.class})
class ReclaimingVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Reclaiming Vines destroys a target artifact")
    void destroysArtifact() {
        destroyTarget(new FountainOfYouth(), "Fountain of Youth");
    }

    @Test
    @DisplayName("Reclaiming Vines destroys a target enchantment")
    void destroysEnchantment() {
        destroyTarget(new AngelicChorus(), "Angelic Chorus");
    }

    @Test
    @DisplayName("Reclaiming Vines destroys a target land")
    void destroysLand() {
        destroyTarget(new Forest(), "Forest");
    }

    @Test
    @DisplayName("Reclaiming Vines cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReclaimingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Permanent creature = findPermanent(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void destroyTarget(Card targetCard, String cardName) {
        harness.addToBattlefield(player2, targetCard);
        harness.setHand(player1, List.of(new ReclaimingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, cardName);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, cardName);
    }
}
