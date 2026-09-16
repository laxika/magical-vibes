package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoundwaveSonicSpy.class, SoundwaveSuperiorCaptain.class, GrizzlyBears.class, Shock.class,
        LightningStrike.class})
class SoundwaveSonicSpyTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsSoundwaveConverted() {
        Permanent soundwave = castConvertedSoundwave();

        assertThat(soundwave.isTransformed()).isTrue();
        assertThat(soundwave.getCard()).isInstanceOf(SoundwaveSuperiorCaptain.class);
        assertThat(gqs.isArtifact(soundwave)).isTrue();
    }

    @Test
    void oddSpellConvertsSoundwaveAndCreatesRavage() {
        Permanent soundwave = castConvertedSoundwave();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(soundwave.isTransformed()).isFalse();
        Permanent ravage = findPermanent(player1, "Ravage");
        assertThat(ravage.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, ravage)).isTrue();
        assertThat(gqs.hasKeyword(gd, ravage, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ravage, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void evenSpellConvertsSoundwaveAndCreatesLaserbeak() {
        Permanent soundwave = castConvertedSoundwave();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(soundwave.isTransformed()).isFalse();
        Permanent laserbeak = findPermanent(player1, "Laserbeak");
        assertThat(laserbeak.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, laserbeak)).isTrue();
        assertThat(gqs.hasKeyword(gd, laserbeak, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, laserbeak, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void tokenCombatDamageCopiesMatchingManaValueSpellAndConvertsAfterCasting() {
        Permanent soundwave = harness.addToBattlefieldAndReturn(player1, new SoundwaveSonicSpy());
        LightningStrike lightningStrike = new LightningStrike();
        harness.setGraveyard(player2, List.of(lightningStrike));

        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = addCreatureReady(player1, tokenCard);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(lightningStrike.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(soundwave.isTransformed()).isTrue();
        assertThat(soundwave.getCard()).isInstanceOf(SoundwaveSuperiorCaptain.class);
    }

    private Permanent castConvertedSoundwave() {
        harness.setHand(player1, List.of(new SoundwaveSonicSpy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Soundwave, Superior Captain");
    }
}
