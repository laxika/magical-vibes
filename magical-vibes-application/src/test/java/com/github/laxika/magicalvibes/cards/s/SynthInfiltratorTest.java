package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynthInfiltrator.class, GrizzlyBears.class})
class SynthInfiltratorTest extends BaseCardTest {

    @Test
    void copyingCreatureAddsArtifactAndSynth() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        SynthInfiltrator infiltrator = new SynthInfiltrator();
        harness.setHand(player1, List.of(infiltrator));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        GameData gameData = harness.getGameData();
        Permanent copy = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(infiltrator.getId()))
                .findFirst()
                .orElse(null);

        assertThat(copy).isNotNull();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.BEAR, CardSubtype.SYNTH);
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void decliningToCopyLeavesNoPermanent() {
        SynthInfiltrator infiltrator = new SynthInfiltrator();
        harness.setHand(player1, List.of(infiltrator));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Synth Infiltrator");
        harness.assertInGraveyard(player1, "Synth Infiltrator");
    }
}
