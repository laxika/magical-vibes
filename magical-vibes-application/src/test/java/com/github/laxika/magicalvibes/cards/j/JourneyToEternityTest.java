package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JourneyToEternityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the enchanted creature and transforms into Atzal when it dies")
    void returnsCreatureAndTransformsOnEnchantedCreatureDeath() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToEternity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent atzal = findPermanent(player1, "Atzal, Cave of Eternity");
        assertThat(atzal.isTransformed()).isTrue();
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotSameAs(creature);
        harness.assertNotInGraveyard(player1, "Journey to Eternity");
    }

    @Test
    @DisplayName("Atzal returns a target creature card from its controller's graveyard")
    void atzalReturnsTargetCreatureFromGraveyard() {
        Permanent atzal = addTransformedAtzal(player1);
        Card creature = new GrizzlyBears();
        gd.playerGraveyards.get(player1.getId()).add(creature);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCard().getId()).isEqualTo(creature.getId());
        assertThat(atzal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Atzal produces a chosen color of mana")
    void atzalProducesChosenColor() {
        Permanent atzal = addTransformedAtzal(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(atzal.isTapped()).isTrue();
    }

    private Permanent addTransformedAtzal(com.github.laxika.magicalvibes.model.Player player) {
        JourneyToEternity card = new JourneyToEternity();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
