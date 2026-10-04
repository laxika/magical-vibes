package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreatHallOfTheBiblioplex.class, Shock.class, ActOfTreason.class})
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
    @DisplayName("The Hall does not reanimate or duplicate its granted ability while already a creature")
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

    @Test
    @DisplayName("Animation remains after turn cleanup and only the spell pump expires")
    void animationPersistsAcrossTurns() {
        Permanent hall = addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, hall)).isTrue();
        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hall)).isEqualTo(4);
    }

    @Test
    @DisplayName("The granted spell-cast ability continues to work on later turns")
    void grantedAbilityPersistsAcrossTurns() {
        Permanent hall = addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Hall grows for spells cast by its new controller")
    void grantedAbilityFollowsNewController() {
        Permanent hall = addHallReady(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ActOfTreason(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, hall.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hall);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Hall does not grow for spells cast by its former controller")
    void grantedAbilityIgnoresFormerController() {
        Permanent hall = addHallReady(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, hall.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hall);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted colored mana can pay for an instant")
    void restrictedManaPaysForInstant() {
        addHallReady(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A sorcery cast by the Hall's controller also grants a pump")
    void sorceryGrantsSpellCastPump() {
        Permanent hall = addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, hall.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hall)).isEqualTo(4);
    }

    @Test
    @DisplayName("Restricted mana cannot pay for the Hall's animation ability")
    void restrictedManaCannotPayForActivation() {
        addHallReady(player1);
        addHallReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unanimated Hall can tap for mana even when newly controlled")
    void noncreatureHallIgnoresSummoningSickness() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new GreatHallOfTheBiblioplex());
        hall.setSummoningSick(true);

        gs.tapPermanent(gd, player1, 0);

        assertThat(hall.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A newly controlled Hall cannot activate either tap ability after animation")
    void animatedHallHasSummoningSickness() {
        Permanent hall = harness.addToBattlefieldAndReturn(player1, new GreatHallOfTheBiblioplex());
        hall.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hall.isTapped()).isFalse();
    }

    private Permanent addHallReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GreatHallOfTheBiblioplex());
        perm.setSummoningSick(false);
        return perm;
    }
}
