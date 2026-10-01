package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfConformity.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class CurseOfConformityTest extends BaseCardTest {

    @Test
    @DisplayName("Nonlegendary creatures controlled by the enchanted player become 3/3s without creature types")
    void transformsNonlegendaryCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).isEmpty();
    }

    @Test
    @DisplayName("Legendary creatures and creatures controlled by other players are unaffected")
    void preservesLegendaryAndOtherPlayersCreatures() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        Permanent otherPlayerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        placeCurseOnPlayer(player1, player2);

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, legendary)).contains(CardSubtype.DOG);
        assertThat(gqs.getEffectivePower(gd, otherPlayerCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherPlayerCreature)).isEqualTo(2);
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = new Permanent(new CurseOfConformity());
        curse.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(curse);
        return curse;
    }
}
