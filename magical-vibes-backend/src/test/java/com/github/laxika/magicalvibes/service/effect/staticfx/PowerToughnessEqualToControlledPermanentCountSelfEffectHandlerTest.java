package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PowerToughnessEqualToControlledPermanentCountSelfEffectHandlerTest extends AbstractStaticEffectHandlerTest {

    private static Permanent createPermanent(String name, CardType type, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(subtypes);
        return new Permanent(card);
    }

    @Test
    @DisplayName("P/T equals count of controlled permanents matching predicate via matchesPermanentPredicate")
    void ptEqualsMatchingControlledPermanentCount() {
        var filter = new PermanentHasSubtypePredicate(CardSubtype.SWAMP);
        var effect = new PowerToughnessEqualToControlledPermanentCountEffect(filter);

        Card sourceCard = new Card();
        sourceCard.setName("Nightmare");
        sourceCard.setType(CardType.CREATURE);
        Permanent source = new Permanent(sourceCard);
        gd.playerBattlefields.get(player1Id).add(source);

        Permanent swamp1 = createPermanent("Swamp", CardType.LAND, List.of(CardSubtype.SWAMP));
        Permanent swamp2 = createPermanent("Swamp", CardType.LAND, List.of(CardSubtype.SWAMP));
        Permanent plains = createPermanent("Plains", CardType.LAND, List.of(CardSubtype.PLAINS));
        gd.playerBattlefields.get(player1Id).add(swamp1);
        gd.playerBattlefields.get(player1Id).add(swamp2);
        gd.playerBattlefields.get(player1Id).add(plains);

        Permanent opponentSwamp = createPermanent("Swamp", CardType.LAND, List.of(CardSubtype.SWAMP));
        gd.playerBattlefields.get(player2Id).add(opponentSwamp);

        when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(swamp1), eq(filter))).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(swamp2), eq(filter))).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(plains), eq(filter))).thenReturn(false);
        when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(source), eq(filter))).thenReturn(false);

        StaticBonusAccumulator accumulator = new StaticBonusAccumulator();
        var context = new StaticEffectContext(source, source, true, gd);
        registry.getSelfHandler(effect).apply(context, effect, accumulator);

        assertThat(accumulator.getPower()).isEqualTo(2);
        assertThat(accumulator.getToughness()).isEqualTo(2);
        verify(gameQueryService, never()).matchesPermanentPredicate(eq(gd), eq(opponentSwamp), any());
    }

    @Test
    @DisplayName("Uses matchesPermanentPredicate for each controlled permanent")
    void usesMatchesPermanentPredicateForEachControlledPermanent() {
        var filter = new PermanentIsArtifactPredicate();
        var effect = new PowerToughnessEqualToControlledPermanentCountEffect(filter);

        Card sourceCard = new Card();
        sourceCard.setName("Darksteel Juggernaut");
        sourceCard.setType(CardType.ARTIFACT);
        sourceCard.setSubtypes(List.of(CardSubtype.JUGGERNAUT));
        Permanent source = new Permanent(sourceCard);
        gd.playerBattlefields.get(player1Id).add(source);

        Permanent artifact = createPermanent("Spellbook", CardType.ARTIFACT, List.of());
        gd.playerBattlefields.get(player1Id).add(artifact);

        when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(source), eq(filter))).thenReturn(true);
        when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(artifact), eq(filter))).thenReturn(true);

        StaticBonusAccumulator accumulator = new StaticBonusAccumulator();
        var context = new StaticEffectContext(source, source, true, gd);
        registry.getSelfHandler(effect).apply(context, effect, accumulator);

        assertThat(accumulator.getPower()).isEqualTo(2);
        assertThat(accumulator.getToughness()).isEqualTo(2);
        verify(gameQueryService).matchesPermanentPredicate(gd, source, filter);
        verify(gameQueryService).matchesPermanentPredicate(gd, artifact, filter);
    }
}
