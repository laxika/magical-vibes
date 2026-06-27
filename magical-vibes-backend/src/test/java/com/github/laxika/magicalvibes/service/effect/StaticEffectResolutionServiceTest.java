package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectHandlerBeanFactory;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaticEffectResolutionServiceTest {

    @Mock private GameQueryService gameQueryService;

    private StaticEffectHandlerRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        StaticEffectSupport support = new StaticEffectSupport(gameQueryService);
        registry = new StaticEffectHandlerRegistry();
        StaticEffectHandlerBeanFactory.registerAll(
                StaticEffectHandlerBeanFactory.createAll(support, gameQueryService, registry),
                registry);

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    @Nested
    @DisplayName("PowerToughnessEqualToControlledPermanentCountEffect")
    class PowerToughnessEqualToControlledPermanentCount {

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

    private static Permanent createPermanent(String name, CardType type, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(subtypes);
        return new Permanent(card);
    }
}
