package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenerousGift.class, GrizzlyBears.class, Forest.class, DarksteelIngot.class})
class GenerousGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any target permanent and creates an Elephant for its controller")
    void destroysPermanentAndCreatesElephantForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castGenerousGift(target);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.GREEN
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ELEPHANT));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Elephant"));
    }

    @Test
    @DisplayName("Creates the Elephant even when the destruction is regenerated")
    void createsElephantWhenDestructionIsRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);
        castGenerousGift(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player2, "Elephant")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new GenerousGift()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates an Elephant for an indestructible permanent's controller")
    void createsElephantWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        castGenerousGift(target);

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        harness.assertNotInGraveyard(player2, "Darksteel Ingot");
        assertThat(findPermanents(player2, "Elephant")).hasSize(1);
        assertThat(findPermanents(player1, "Elephant")).isEmpty();
    }

    @Test
    @DisplayName("Can destroy your own permanent and give you the Elephant")
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        castGenerousGift(target);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanents(player1, "Elephant")).hasSize(1);
        assertThat(findPermanents(player2, "Elephant")).isEmpty();
    }

    @Test
    @DisplayName("Does not create an extra Elephant when its target has already been destroyed")
    void doesNotCreateTokenWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GenerousGift(), new GenerousGift()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(findPermanents(player2, "Elephant")).hasSize(1);
        assertThat(findPermanents(player1, "Elephant")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castGenerousGift(Permanent target) {
        harness.setHand(player1, List.of(new GenerousGift()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
