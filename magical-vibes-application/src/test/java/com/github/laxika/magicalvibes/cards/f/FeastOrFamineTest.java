package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.cards.k.KrovikanHorror;
import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({FeastOrFamine.class, ElvishRanger.class, GorillaChieftain.class, KrovikanHorror.class,
        SchoolOfTheUnseen.class, ShieldSphere.class})
class FeastOrFamineTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 1 creates a 2/2 black Zombie token")
    void createsZombieToken() {
        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getEffectivePower()).isEqualTo(2);
        assertThat(zombie.getEffectiveToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        harness.assertInGraveyard(player1, "Feast or Famine");
    }

    @Test
    @DisplayName("Mode 2 destroys a nonartifact, nonblack creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new ElvishRanger());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent ranger = findPermanent(player2, "Elvish Ranger");

        harness.castInstant(player1, 0, 1, ranger.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elvish Ranger");
        harness.assertInGraveyard(player2, "Elvish Ranger");
    }

    @Test
    @DisplayName("Mode 2 cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new KrovikanHorror());
        harness.addToBattlefield(player1, new ElvishRanger());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent horror = findPermanent(player2, "Krovikan Horror");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, horror.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new ShieldSphere());
        harness.addToBattlefield(player1, new ElvishRanger());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent sphere = findPermanent(player2, "Shield Sphere");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, sphere.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SchoolOfTheUnseen());
        harness.addToBattlefield(player1, new ElvishRanger());

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent school = findPermanent(player2, "School of the Unseen");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, school.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 2 destroys a creature even when it has a regeneration shield")
    void cannotBeRegenerated() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player2, new GorillaChieftain());
        chieftain.setRegenerationShield(1);

        harness.setHand(player1, List.of(new FeastOrFamine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 1, chieftain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gorilla Chieftain");
        harness.assertInGraveyard(player2, "Gorilla Chieftain");
    }
}
