package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DeeprootWarrior;
import com.github.laxika.magicalvibes.cards.d.DiveDown;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.a.AncientBrontodon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkThePlank.class, DeeprootWarrior.class, AncientBrontodon.class, DiveDown.class, Swamp.class})
class WalkThePlankTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Walk the Plank targeting a non-Merfolk creature puts it on stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(WalkThePlank.class);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a Merfolk creature")
    void cannotTargetMerfolkCreature() {
        // Add a non-Merfolk creature as valid target so spell is playable
        harness.addToBattlefield(player1, new AncientBrontodon());

        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new DeeprootWarrior());

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Merfolk");
    }

    @Test
    @DisplayName("Resolving Walk the Plank destroys target creature")
    void resolvingDestroysTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Ancient Brontodon");
        harness.assertInGraveyard(player2, "Ancient Brontodon");
        harness.assertInGraveyard(player1, "Walk the Plank");
    }

    @Test
    @DisplayName("Walk the Plank allows regeneration")
    void allowsRegeneration() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Ancient Brontodon");
        harness.assertNotInGraveyard(player2, "Ancient Brontodon");
    }

    @Test
    @DisplayName("Walk the Plank fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());

        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, bears.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Walk the Plank");
    }

    @Test
    @DisplayName("Walk the Plank can destroy its controller's creature")
    void destroysOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AncientBrontodon());
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Ancient Brontodon");
        harness.assertInGraveyard(player1, "Ancient Brontodon");
        harness.assertInGraveyard(player1, "Walk the Plank");
    }

    @Test
    @DisplayName("Walk the Plank cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new AncientBrontodon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Swamp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Walk the Plank does not destroy a creature that becomes a Merfolk before resolution")
    void targetGainsMerfolkBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, creature.getId());

        creature.getGrantedSubtypes().add(CardSubtype.MERFOLK);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ancient Brontodon");
        harness.assertNotInGraveyard(player2, "Ancient Brontodon");
        harness.assertInGraveyard(player1, "Walk the Plank");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Walk the Plank cannot destroy an indestructible creature")
    void indestructibleCreatureSurvives() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Ancient Brontodon");
        harness.assertNotInGraveyard(player2, "Ancient Brontodon");
        harness.assertInGraveyard(player1, "Walk the Plank");
    }

    @Test
    @DisplayName("Dive Down makes Walk the Plank's target illegal before resolution")
    void diveDownProtectsTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
        harness.setHand(player1, List.of(new WalkThePlank()));
        harness.setHand(player2, List.of(new DiveDown()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ancient Brontodon");
        harness.assertNotInGraveyard(player2, "Ancient Brontodon");
        harness.assertInGraveyard(player2, "Dive Down");
        harness.assertInGraveyard(player1, "Walk the Plank");
        assertThat(gd.stack).isEmpty();
    }
}
