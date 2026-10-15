package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.d.Damnation;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Saltblast;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RebuffTheWicked.class, BloodKnight.class, Damnation.class, Pongify.class, Saltblast.class,
        UrborgTombOfYawgmoth.class, ProdigalPyromancer.class})
class RebuffTheWickedTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell that targets a permanent you control")
    void countersSpellTargetingYourPermanent() {
        var targetId = harness.addToBattlefieldAndReturn(player2, new BloodKnight()).getId();

        Pongify pongify = new Pongify();
        harness.setHand(player1, List.of(pongify));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, pongify.getId());

        harness.assertInGraveyard(player1, "Pongify");
        harness.assertOnBattlefield(player2, "Blood Knight");
        harness.assertInGraveyard(player2, "Rebuff the Wicked");
    }

    @Test
    @DisplayName("Cannot target a spell that targets an opponent's permanent")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        var targetId = harness.addToBattlefieldAndReturn(player1, new BloodKnight()).getId();

        Pongify pongify = new Pongify();
        harness.setHand(player1, List.of(pongify));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, pongify.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell that does not target a permanent")
    void cannotTargetNonTargetingSpell() {
        Damnation damnation = new Damnation();
        harness.setHand(player1, List.of(damnation));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, damnation.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a spell that targets a noncreature permanent you control")
    void countersSpellTargetingNoncreaturePermanentYouControl() {
        var targetId = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth()).getId();

        Saltblast saltblast = new Saltblast();
        harness.setHand(player1, List.of(saltblast));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, saltblast.getId());

        harness.assertInGraveyard(player1, "Saltblast");
        harness.assertOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Rebuff the Wicked");
    }

    @Test
    @DisplayName("Can counter your own spell targeting your permanent")
    void countersYourOwnSpell() {
        var targetId = harness.addToBattlefieldAndReturn(player1, new BloodKnight()).getId();
        Pongify pongify = new Pongify();
        harness.setHand(player1, List.of(pongify, new RebuffTheWicked()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, pongify.getId());

        harness.assertOnBattlefield(player1, "Blood Knight");
        harness.assertInGraveyard(player1, "Pongify");
        harness.assertInGraveyard(player1, "Rebuff the Wicked");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not counter the spell if its only targeted permanent has left the battlefield")
    void losesLegalTargetWhenPermanentLeavesBattlefield() {
        var targetId = harness.addToBattlefieldAndReturn(player2, new BloodKnight()).getId();
        Pongify original = new Pongify();
        harness.setHand(player1, List.of(original, new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, original.getId());
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Blood Knight");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rebuff the Wicked");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(original.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter an activated ability targeting your permanent")
    void cannotTargetActivatedAbility() {
        var pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        var targetId = harness.addToBattlefieldAndReturn(player2, new BloodKnight()).getId();
        harness.setHand(player2, List.of(new RebuffTheWicked()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, targetId);
        var abilityId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Rebuff the Wicked");
        assertThat(gd.stack).hasSize(1);
    }
}
