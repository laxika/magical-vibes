package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GisaGloriousResurrector.class, GrizzlyBears.class, Shock.class})
class GisaGloriousResurrectorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's dying creature with Gisa")
    void exilesOpponentCreatureWithGisa() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyCreature(player1, bears.getId());

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getCard().getId()).sourcePermanentId())
                .isEqualTo(gisa.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns exiled creatures under Gisa's control with decayed at upkeep")
    void returnsExiledCreatureWithDecayedAtUpkeep() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyCreature(player1, bears.getId());

        triggerUpkeep(player1);

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        assertThat(returned).isNotNull();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DECAYED)).isTrue();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
        assertThat(gisa.getId()).isNotEqualTo(returned.getId());
    }

    @Test
    @DisplayName("Does not replace the controller's own dying creature")
    void doesNotReplaceOwnCreatureDeath() {
        harness.addToBattlefield(player1, new GisaGloriousResurrector());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyCreature(player2, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    private void destroyCreature(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
