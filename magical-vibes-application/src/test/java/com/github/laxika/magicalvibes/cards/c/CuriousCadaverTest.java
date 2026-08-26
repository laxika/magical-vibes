package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriousCadaver.class, Forest.class})
class CuriousCadaverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Clue returns Curious Cadaver from the graveyard to its owner's hand")
    void sacrificingClueReturnsSelfFromGraveyard() {
        CuriousCadaver cadaver = new CuriousCadaver();
        harness.setGraveyard(player1, List.of(cadaver));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Curious Cadaver");
        harness.assertNotInGraveyard(player1, "Curious Cadaver");
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not return Curious Cadaver")
    void sacrificingNonClueDoesNotReturnSelf() {
        harness.setGraveyard(player1, List.of(new CuriousCadaver()));
        Permanent artifact = addArtifactToken(player1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(artifact), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Curious Cadaver");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> "Curious Cadaver".equals(card.getName()));
    }

    private Permanent addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setManaCost("");
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        clueCard.addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice this token: Draw a card."
        ));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
        return clue;
    }

    private Permanent addArtifactToken(Player player) {
        Card artifactCard = new Card();
        artifactCard.setName("Artifact Token");
        artifactCard.setType(CardType.ARTIFACT);
        artifactCard.setManaCost("");
        artifactCard.setToken(true);
        artifactCard.addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost()),
                "Sacrifice this token."
        ));
        Permanent artifact = new Permanent(artifactCard);
        artifact.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(artifact);
        return artifact;
    }
}
