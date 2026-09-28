package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CactusPreserve.class, GrizzlyBears.class, Wasteland.class})
class CactusPreserveTest extends BaseCardTest {

    @Test
    @DisplayName("Cactus Preserve enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new CactusPreserve()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Cactus Preserve").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability can add colorless mana that a land you control could produce")
    void addsColorlessManaFromControlledLand() {
        addCactusReady(player1);
        harness.addToBattlefield(player1, new Wasteland());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animation uses the greatest mana value among the controller's commanders")
    void animationUsesGreatestCommanderManaValue() {
        prepareCommander();
        Permanent cactus = addCactusReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, cactus)).isTrue();
        assertThat(gqs.isCreature(gd, cactus)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cactus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cactus)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, cactus)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, cactus)).contains(CardSubtype.PLANT);
        assertThat(gqs.hasKeyword(gd, cactus, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cactus)).isFalse();
    }

    private Permanent addCactusReady(Player player) {
        return addCreatureReady(player, new CactusPreserve());
    }

    private void prepareCommander() {
        Card commander = new GrizzlyBears();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
    }
}
