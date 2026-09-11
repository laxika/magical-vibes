package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DrownerOfHope.class, Forest.class, GrizzlyBears.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        int drownerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drowner);

        assertThatThrownBy(() -> harness.activateAbility(player1, drownerIndex, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castDrownerOfHope() {
        harness.setHand(player1, List.of(new DrownerOfHope()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
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
