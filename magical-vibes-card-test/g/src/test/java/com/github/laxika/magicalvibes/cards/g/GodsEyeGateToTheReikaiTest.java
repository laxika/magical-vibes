package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodsEyeGateToTheReikai.class, StoneRain.class})
class GodsEyeGateToTheReikaiTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new GodsEyeGateToTheReikai());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a colorless 1/1 Spirit token when put into a graveyard from the battlefield")
    void createsColorlessSpiritWhenDestroyed() {
        Permanent godsEye = harness.addToBattlefieldAndReturn(player1, new GodsEyeGateToTheReikai());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, godsEye.getId());

        harness.assertInGraveyard(player1, "Gods' Eye, Gate to the Reikai");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        Permanent spirit = spirits.getFirst();
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getColor()).isNull();
        assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent destroying Gods' Eye gives its controller the Spirit after the trigger resolves")
    void opponentDestroyingLandCreatesSpiritForItsController() {
        Permanent godsEye = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, godsEye.getId());

        harness.assertInGraveyard(player2, "Gods' Eye, Gate to the Reikai");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
