package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DrippingTongueZubera;
import com.github.laxika.magicalvibes.cards.t.Thoughtbind;
import com.github.laxika.magicalvibes.cards.m.ManaReflection;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoseijuWhoSheltersAll.class, CounselOfTheSoratami.class, DrippingTongueZubera.class,
        Thoughtbind.class, YamabushisFlame.class, ManaReflection.class})
class BoseijuWhoSheltersAllTest extends BaseCardTest {

    private void addBoseiju(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new BoseijuWhoSheltersAll());
        }
    }

    @Test
    @DisplayName("Boseiju enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BoseijuWhoSheltersAll()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Boseiju, Who Shelters All").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating taps Boseiju, pays 2 life and adds {C}")
    void activatingAddsColorlessAndPaysLife() {
        addBoseiju(1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        Permanent boseiju = findPermanent(player1, "Boseiju, Who Shelters All");
        assertThat(boseiju.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.getUncounterableGrantingManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A sorcery paid for with Boseiju's mana can't be countered")
    void sorceryPaidWithBoseijuManaCannotBeCountered() {
        addBoseiju(1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setLibrary(player1, List.of(new DrippingTongueZubera(), new DrippingTongueZubera()));
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, counsel.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Thoughtbind");
        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same sorcery paid for with ordinary mana is countered")
    void sorceryPaidWithOrdinaryManaIsCountered() {
        harness.setLibrary(player1, List.of(new DrippingTongueZubera(), new DrippingTongueZubera()));
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, counsel, "{2}{U}");

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, counsel.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An instant paid for with Boseiju's mana can't be countered")
    void instantPaidWithBoseijuManaCannotBeCountered() {
        addBoseiju(1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        YamabushisFlame flame = new YamabushisFlame();
        harness.setHand(player1, List.of(flame));

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, player2.getId());

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, flame.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Thoughtbind");
        harness.assertInGraveyard(player1, "Yamabushi's Flame");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boseiju's mana spent on a creature spell leaves that spell counterable")
    void creatureSpellPaidWithBoseijuManaIsStillCounterable() {
        addBoseiju(1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);

        DrippingTongueZubera zubera = new DrippingTongueZubera();
        harness.setHand(player1, List.of(zubera));

        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, zubera.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Thoughtbind");
        harness.assertInGraveyard(player1, "Dripping-Tongue Zubera");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boseiju cannot activate when its controller has less than two life")
    void cannotPayLifeCostWithOneLife() {
        addBoseiju(1);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        assertThat(findPermanent(player1, "Boseiju, Who Shelters All").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Boseiju cannot activate again")
    void cannotActivateTwiceWithoutUntapping() {
        addBoseiju(1);
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boseiju mana spent on a creature does not protect a later sorcery")
    void protectionDoesNotCarryOverAfterManaIsSpent() {
        addBoseiju(1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new DrippingTongueZubera()));
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new DrippingTongueZubera(), new DrippingTongueZubera()));
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.castFromHand(player1, counsel, "{2}{U}");
        harness.setHand(player2, List.of(new Thoughtbind()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, counsel.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Thoughtbind");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({BoseijuWhoSheltersAll.class, ManaReflection.class})
    @DisplayName("Mana Reflection doubles Boseiju mana and preserves its rider")
    void manaReflectionDoublesManaWithItsRider() {
        addBoseiju(1);
        harness.addToBattlefield(player1, new ManaReflection());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 18);
        assertThat(findPermanent(player1, "Boseiju, Who Shelters All").isTapped()).isTrue();
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(pool.getUncounterableGrantingManaTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
