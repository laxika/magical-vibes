package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreatHallOfTheBiblioplexTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping the Hall adds colorless mana")
    void tappingAddsColorlessMana() {
        Permanent hall = addHallReady(player1);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(hall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying life adds colored mana restricted to instant and sorcery spells")
    void payingLifeAddsRestrictedMana() {
        addHallReady(player1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animating the Hall creates a Wizard that grows when its controller casts an instant")
    void animatingGrantsSpellCastPump() {
        Permanent hall = addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hall)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hall)).isEqualTo(4);
        assertThat(hall.getTransientSubtypes()).containsExactly(CardSubtype.WIZARD);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Hall does not reanimate or duplicate its temporary ability while already a creature")
    void doesNotAnimateAgainWhileAlreadyCreature() {
        Permanent hall = addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(3);
    }

    private Permanent addHallReady(Player player) {
        Permanent perm = new Permanent(new GreatHallOfTheBiblioplex());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
