package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OonasProwler;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiscreetRetreat.class, Forest.class, GrizzlyBears.class, OonasProwler.class})
class DiscreetRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land adds two mana of one chosen color in the outlaw bucket")
    void enchantedLandAddsRestrictedMana() {
        attachToForest();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(
                Set.of(CardSubtype.OUTLAW), ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(
                Set.of(CardSubtype.OUTLAW), ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Outlaw mana can cast an outlaw spell")
    void restrictedManaChecksOutlawSpells() {
        attachToForest();
        addOutlawMana(ManaColor.BLACK);

        harness.setHand(player1, List.of(new OonasProwler()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Outlaw mana cannot cast another creature spell")
    void restrictedManaRejectsNonOutlawSpells() {
        attachToForest();
        addOutlawMana(ManaColor.GREEN);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Outlaw mana can activate an outlaw source's ability")
    void restrictedManaPaysOutlawAbility() {
        attachToForest();
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        TestCards.mutableCard(outlaw).setSubtypes(List.of(CardSubtype.ROGUE));
        outlaw.getCard().addActivatedAbility(new ActivatedAbility(
                false, "{G}", List.of(new GainLifeEffect(1)), "{G}: You gain 1 life."));

        addOutlawMana(ManaColor.GREEN);
        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Only the first outlaw spell each turn draws and costs life")
    void triggersOnlyForFirstOutlawSpellEachTurn() {
        attachToForest();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new OonasProwler(), new OonasProwler()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-outlaw spell does not consume the first outlaw trigger")
    void nonOutlawDoesNotConsumeTrigger() {
        attachToForest();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new OonasProwler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An outlaw cast before the Aura enters still counts as the first outlaw")
    void earlierOutlawPreventsTrigger() {
        harness.setHand(player1, List.of(new OonasProwler(), new OonasProwler()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        attachToForest();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's enchanted land gives mana to its controller")
    void opponentControlsGrantedManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DiscreetRetreat()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.OUTLAW), ManaColor.BLUE))
                .isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.OUTLAW), ManaColor.BLUE))
                .isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    private Permanent attachToForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DiscreetRetreat());
        aura.setAttachedTo(forest.getId());
        return forest;
    }

    private void addOutlawMana(ManaColor color) {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());
    }
}
