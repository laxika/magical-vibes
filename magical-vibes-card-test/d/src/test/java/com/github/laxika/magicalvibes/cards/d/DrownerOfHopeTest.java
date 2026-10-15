package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OranRiefInvoker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownerOfHope.class, Island.class, OranRiefInvoker.class})
class DrownerOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("When Drowner of Hope enters, it creates two Eldrazi Scion tokens")
    void enteringCreatesTwoEldraziScions() {
        castDrownerOfHope();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("An Eldrazi Scion can be sacrificed to add colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        castDrownerOfHope();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing an Eldrazi Scion taps a target creature")
    void sacrificingScionTapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        castDrownerOfHope();

        Permanent drowner = findPermanent(player1, "Drowner of Hope");
        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int drownerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);

        harness.activateAbility(player1, drownerIndex, 0, null, target.getId());
        harness.handlePermanentChosen(player1, scion.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot sacrifice an Eldrazi Spawn")
    void cannotSacrificeEldraziSpawn() {
        Permanent drowner = harness.addToBattlefieldAndReturn(player1, new DrownerOfHope());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        harness.addToBattlefield(player1, createToken("Eldrazi Spawn", CardSubtype.SPAWN));
        int drownerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);

        assertThatThrownBy(() -> harness.activateAbility(player1, drownerIndex, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent drowner = harness.addToBattlefieldAndReturn(player1, new DrownerOfHope());
        harness.addToBattlefield(player1, createToken("Eldrazi Scion", CardSubtype.SCION));
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        int drownerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);

        assertThatThrownBy(() -> harness.activateAbility(player1, drownerIndex, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Created Scions are untapped colorless 1/1 Eldrazi Scion creatures")
    void createdScionsHaveCorrectCharacteristics() {
        castDrownerOfHope();

        for (Permanent scion : findPermanents(player1, "Eldrazi Scion")) {
            assertThat(scion.isTapped()).isFalse();
            assertThat(scion.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(gqs.getEffectiveColors(gd, scion)).isEmpty();
            assertThat(scion.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);
            assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("The Scion sacrifice is paid before the tap ability resolves and adds no mana")
    void sacrificeIsPaidBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefInvoker());
        castDrownerOfHope();
        Permanent drowner = findPermanent(player1, "Drowner of Hope");
        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drowner),
                0, null, target.getId());
        harness.handlePermanentChosen(player1, scion.getId());

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Drowner can sacrifice a tapped Scion to tap its controller's creature")
    void tappedSourceAndScionCanTapOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OranRiefInvoker());
        castDrownerOfHope();
        Permanent drowner = findPermanent(player1, "Drowner of Hope");
        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        drowner.tap();
        scion.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drowner),
                0, null, target.getId());
        harness.handlePermanentChosen(player1, scion.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("A newly created tapped Scion can add mana immediately without using the stack")
    void tappedNewScionProducesManaImmediately() {
        castDrownerOfHope();
        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        scion.tap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scion),
                0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Scions cannot pay Drowner's sacrifice cost")
    void cannotSacrificeOpponentsScions() {
        Permanent drowner = harness.addToBattlefieldAndReturn(player1, new DrownerOfHope());
        Permanent opponentDrowner = harness.enterBattlefieldAndReturn(player2, new DrownerOfHope());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Eldrazi Scion")).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(drowner), 0, null, opponentDrowner.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player2, "Eldrazi Scion")).hasSize(2);
    }

    private void castDrownerOfHope() {
        harness.setHand(player1, List.of(new DrownerOfHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Card createToken(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(0);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.ELDRAZI, subtype));
        return card;
    }
}
