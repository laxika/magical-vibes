package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.c.ChimericStaff;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmokeSpiritsAid.class, GrizzlyBears.class, Shock.class, FountainOfYouth.class,
        ChildOfNight.class, ChimericStaff.class})
class SmokeSpiritsAidTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one red Smoke Blessing Aura for each chosen creature")
    void createsAuraTokensAttachedToUpToXCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmokeSpiritsAid()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        List<Permanent> blessings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Smoke Blessing"))
                .toList();
        assertThat(blessings).hasSize(2);
        assertThat(blessings).allSatisfy(blessing -> {
            assertThat(blessing.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(blessing.getCard().getSubtypes()).containsExactly(CardSubtype.AURA);
        });
        assertThat(blessings).extracting(Permanent::getAttachedTo)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    @DisplayName("A Smoke Blessing deals damage to the enchanted creature's controller and creates a Treasure when it dies")
    void enchantedCreatureDeathDealsDamageAndCreatesTreasure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmokeSpiritsAid(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treasure")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent blessingTarget = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SmokeSpiritsAid()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(blessingTarget.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("X can be zero and no Smoke Blessings are created")
    void zeroXCreatesNoTokens() {
        harness.setHand(player1, List.of(new SmokeSpiritsAid()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Smoke Blessing");
        harness.assertInGraveyard(player1, "Smoke Spirits' Aid");
    }

    @Test
    @DisplayName("Choosing fewer than X targets creates only one Aura per chosen creature")
    void fewerThanXTargetsCreateOnlyChosenBlessings() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmokeSpiritsAid()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Smoke Blessing")))
                .singleElement()
                .satisfies(blessing -> assertThat(blessing.getAttachedTo()).isEqualTo(chosen.getId()));
    }

    @Test
    @DisplayName("A target that dies before resolution receives no Aura while the remaining target does")
    void resolvesForRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmokeSpiritsAid(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2, List.of(removed.getId(), remaining.getId()));
        harness.castAndResolveInstant(player1, 0, removed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Smoke Blessing")))
                .singleElement()
                .satisfies(blessing -> assertThat(blessing.getAttachedTo()).isEqualTo(remaining.getId()));
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The dying creature deals Smoke Blessing's damage using its last-known lifelink")
    void dyingCreatureLifelinkGainsLifeForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChildOfNight());
        harness.setHand(player1, List.of(new SmokeSpiritsAid(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treasure")))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("Smoke Blessing is removed when its enchanted permanent stops being a creature")
    void blessingFallsOffWhenAnimationEnds() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new ChimericStaff());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SmokeSpiritsAid()));

        harness.castSorcery(player1, 0, 1, List.of(staff.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Smoke Blessing");
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, staff)).isFalse();
        harness.assertOnBattlefield(player1, "Chimeric Staff");
        harness.assertNotOnBattlefield(player1, "Smoke Blessing");
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.assertLife(player1, 20);
    }
}
