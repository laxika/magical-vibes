package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.a.Atog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cryptothrall.class, AlphaMyr.class, AetherSpellbomb.class, Atog.class})
class CryptothrallTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other artifact creatures you control hexproof")
    void givesOtherArtifactCreaturesYouControlHexproof() {
        Permanent cryptothrall = harness.addToBattlefieldAndReturn(player1, new Cryptothrall());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new AlphaMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());
        Permanent nonartifactCreature = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent opponentArtifactCreature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, cryptothrall, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonartifactCreature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.HEXPROOF)).isFalse();
    }
}
