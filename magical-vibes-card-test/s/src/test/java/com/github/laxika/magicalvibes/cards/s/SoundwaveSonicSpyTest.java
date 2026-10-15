package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
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
        LightningStrike.class, Sleep.class})
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

    @Test
    void castingTheCopyConvertsSoundwaveBeforePlayersReceivePriority() {
        Permanent soundwave = harness.addToBattlefieldAndReturn(player1, new SoundwaveSonicSpy());
        LightningStrike spell = new LightningStrike();
        harness.setGraveyard(player2, List.of(spell));
        Permanent token = addBearTokenReady();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(soundwave.isTransformed()).isTrue();
        assertThat(soundwave.getCard()).isInstanceOf(SoundwaveSuperiorCaptain.class);
        resolveAllTriggers();
        harness.assertLife(player2, 15);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void decliningTheCopyKeepsSoundwaveOnItsFrontFaceAndTheOriginalExiled() {
        Permanent soundwave = harness.addToBattlefieldAndReturn(player1, new SoundwaveSonicSpy());
        LightningStrike spell = new LightningStrike();
        harness.setGraveyard(player2, List.of(spell));
        Permanent token = addBearTokenReady();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(soundwave.isTransformed()).isFalse();
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Lightning Strike");
        harness.assertLife(player2, 18);
    }

    @Test
    void simultaneousTokenDamageUsesTheTotalAndOffersOnlyOneCopy() {
        Permanent soundwave = harness.addToBattlefieldAndReturn(player1, new SoundwaveSonicSpy());
        Sleep spell = new Sleep();
        LightningStrike wrongManaValue = new LightningStrike();
        harness.setGraveyard(player2, List.of(spell, wrongManaValue));
        Permanent first = addBearTokenReady();
        Permanent second = addBearTokenReady();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(soundwave.isTransformed()).isFalse();
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertInGraveyard(player2, "Lightning Strike");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 16);
    }

    @Test
    void nontokenCombatDamageDoesNotTriggerSoundwave() {
        harness.addToBattlefield(player1, new SoundwaveSonicSpy());
        harness.setGraveyard(player2, List.of(new LightningStrike()));
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Lightning Strike");
        harness.assertLife(player2, 18);
    }

    @Test
    void tokenDamageCannotExileASpellWithTheWrongManaValue() {
        harness.addToBattlefield(player1, new SoundwaveSonicSpy());
        harness.setGraveyard(player2, List.of(new Shock()));
        Permanent token = addBearTokenReady();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void tokenDamageCannotTargetYourGraveyardOrACreatureCard() {
        harness.addToBattlefield(player1, new SoundwaveSonicSpy());
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        Permanent token = addBearTokenReady();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(token)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Lightning Strike");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void opponentsSpellDoesNotConvertSoundwaveOrCreateAToken() {
        Permanent soundwave = castConvertedSoundwave();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(soundwave.isTransformed()).isTrue();
        assertThat(findPermanents(player1, "Ravage")).isEmpty();
        assertThat(findPermanents(player1, "Laserbeak")).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void stackedCastTriggersConvertOnlyOnceAndCreateOnlyTheFirstResolvingToken() {
        Permanent soundwave = castConvertedSoundwave();
        harness.setHand(player1, List.of(new Shock(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(soundwave.isTransformed()).isFalse();
        assertThat(findPermanents(player1, "Laserbeak")).hasSize(1);
        assertThat(findPermanents(player1, "Ravage")).isEmpty();
        harness.assertLife(player2, 15);
    }

    private Permanent addBearTokenReady() {
        GrizzlyBears card = new GrizzlyBears();
        card.setToken(true);
        return addCreatureReady(player1, card);
    }

    private Permanent castConvertedSoundwave() {
        harness.setHand(player1, List.of(new SoundwaveSonicSpy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        return findPermanent(player1, "Soundwave, Superior Captain");
    }
}
