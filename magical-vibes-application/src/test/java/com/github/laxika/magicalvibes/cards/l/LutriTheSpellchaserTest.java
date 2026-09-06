package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DoomedNecromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LutriTheSpellchaser.class, Shock.class, CounselOfTheSoratami.class,
        GrizzlyBears.class, DoomedNecromancer.class})
class LutriTheSpellchaserTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Lutri copies an instant you control")
    void castLutriCopiesInstant() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new LutriTheSpellchaser()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shock.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Lutri can copy a sorcery without targets")
    void copiesSorceryWithoutTargets() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel, new LutriTheSpellchaser()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, counsel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size() - handBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("Lutri cannot copy a creature spell")
    void cannotCopyCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new LutriTheSpellchaser()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lutri does not trigger when put onto the battlefield without being cast")
    void nonCastEntryDoesNotTrigger() {
        LutriTheSpellchaser lutri = new LutriTheSpellchaser();
        DoomedNecromancer necromancer = new DoomedNecromancer();
        Permanent necromancerPermanent = new Permanent(necromancer);
        necromancerPermanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(necromancerPermanent);
        harness.setGraveyard(player1, List.of(lutri));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }
}
