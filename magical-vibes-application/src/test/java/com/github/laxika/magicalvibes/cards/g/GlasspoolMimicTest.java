package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlasspoolMimic.class, GlasspoolShore.class, GrizzlyBears.class})
class GlasspoolMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Can copy a creature you control and keeps its copy exception subtypes")
    void copiesCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlasspoolMimic()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearId);

        Permanent mimic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Glasspool Mimic"))
                .findFirst()
                .orElseThrow();
        assertThat(mimic.getCard().getPower()).isEqualTo(2);
        assertThat(mimic.getCard().getToughness()).isEqualTo(2);
        assertThat(mimic.getCard().getSubtypes())
                .contains(CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Cannot copy a creature controlled by an opponent")
    void cannotCopyOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GlasspoolMimic()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getName().equals("Glasspool Mimic"));
        harness.assertInGraveyard(player1, "Glasspool Mimic");
    }

    @Test
    @DisplayName("Glasspool Shore enters tapped and produces blue mana")
    void landFaceEntersTappedAndProducesBlueMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlasspoolMimic()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
    }
}
