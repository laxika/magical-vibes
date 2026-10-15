package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mistfolk.class, ArcTrail.class, GrizzlyBears.class, Shock.class, ZuranSpellcaster.class})
class MistfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting Mistfolk")
    void countersSpellTargetingSelf() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mistfolk"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Mistfolk");
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a multi-target spell that targets Mistfolk")
    void countersMultiTargetSpellTargetingSelf() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        ArcTrail arcTrail = new ArcTrail();
        harness.setHand(player2, List.of(arcTrail));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0,
                List.of(harness.getPermanentId(player1, "Mistfolk"), player2.getId()));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, arcTrail.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arc Trail");
        harness.assertOnBattlefield(player1, "Mistfolk");
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a spell targeting another creature")
    void cannotTargetSpellTargetingOtherCreature() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters the entire spell when Mistfolk is its second target")
    void countersSpellWithSelfAsSecondTarget() {
        harness.addToBattlefield(player1, new Mistfolk());
        ArcTrail arcTrail = new ArcTrail();
        harness.setHand(player2, List.of(arcTrail));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0,
                List.of(player1.getId(), harness.getPermanentId(player1, "Mistfolk")));
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, arcTrail.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Mistfolk");
        harness.assertInGraveyard(player2, "Arc Trail");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-targeting spell")
    void cannotTargetNonTargetingSpell() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without {U}")
    void cannotActivateWithoutBlueMana() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mistfolk"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate repeatedly without tapping Mistfolk")
    void canActivateRepeatedlyWithoutTapping() {
        Mistfolk mistfolk = new Mistfolk();
        harness.addToBattlefield(player1, mistfolk);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Mistfolk"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Mistfolk");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Can counter its controller's spell targeting Mistfolk")
    void countersOwnSpellTargetingSelf() {
        harness.addToBattlefield(player1, new Mistfolk());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Mistfolk"));
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player1, "Mistfolk");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter an activated ability targeting Mistfolk")
    void cannotTargetActivatedAbility() {
        harness.addToBattlefield(player1, new Mistfolk());
        var spellcaster = harness.addToBattlefieldAndReturn(player2, new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, harness.getPermanentId(player1, "Mistfolk"));
        var abilityId = harness.getGameData().stack.getLast().getTargetableId();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mistfolk");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability still counters its spell after Mistfolk dies")
    void countersSpellAfterSourceDies() {
        harness.addToBattlefield(player1, new Mistfolk());
        var mistfolkId = harness.getPermanentId(player1, "Mistfolk");
        Shock firstShock = new Shock();
        Shock secondShock = new Shock();
        harness.setHand(player2, List.of(firstShock, secondShock));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, mistfolkId);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, firstShock.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, mistfolkId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mistfolk");
        assertThat(harness.getGameData().stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .contains(firstShock, secondShock);
    }
}
