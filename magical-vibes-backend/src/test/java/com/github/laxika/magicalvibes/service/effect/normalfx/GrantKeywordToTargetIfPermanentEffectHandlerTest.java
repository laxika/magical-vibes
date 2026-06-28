package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetIfPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.GrantKeywordToTargetIfPermanentEffectHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrantKeywordToTargetIfPermanentEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;

    private GrantKeywordToTargetIfPermanentEffectHandler handler;
    private EffectHandlerRegistry registry;
    private GameData gd;
    private UUID player1Id;

    @BeforeEach
    void setUp() {
        handler = new GrantKeywordToTargetIfPermanentEffectHandler(gameQueryService, gameBroadcastService);
        registry = new EffectHandlerRegistry();
        registry.register(handler.handledEffect(), handler);

        player1Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("Grants keyword when target matches predicate")
    void grantsKeywordWhenTargetMatchesPredicate() {
        var predicate = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        var effect = new GrantKeywordToTargetIfPermanentEffect(Keyword.FIRST_STRIKE, predicate);

        Card vampire = new Card();
        vampire.setName("Vampire Interloper");
        vampire.setType(CardType.CREATURE);
        vampire.setSubtypes(List.of(CardSubtype.VAMPIRE));
        Permanent target = new Permanent(vampire);
        gd.playerBattlefields.get(player1Id).add(target);

        Card spell = new Card();
        spell.setName("Vampire's Zeal");
        StackEntry entry = new StackEntry(
                StackEntryType.INSTANT_SPELL, spell, player1Id, spell.getName(), List.of(effect), 0, target.getId(), null);

        when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
        when(gameQueryService.matchesPermanentPredicate(gd, target, predicate)).thenReturn(true);

        registry.getHandler(effect).resolve(gd, entry, effect);

        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        verify(gameBroadcastService).logAndBroadcast(eq(gd), anyString());
    }

    @Test
    @DisplayName("Does not grant keyword when target does not match predicate")
    void doesNotGrantKeywordWhenTargetDoesNotMatchPredicate() {
        var predicate = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        var effect = new GrantKeywordToTargetIfPermanentEffect(Keyword.FIRST_STRIKE, predicate);

        Card bear = new Card();
        bear.setName("Grizzly Bears");
        bear.setType(CardType.CREATURE);
        Permanent target = new Permanent(bear);
        gd.playerBattlefields.get(player1Id).add(target);

        Card spell = new Card();
        spell.setName("Vampire's Zeal");
        StackEntry entry = new StackEntry(
                StackEntryType.INSTANT_SPELL, spell, player1Id, spell.getName(), List.of(effect), 0, target.getId(), null);

        when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
        when(gameQueryService.matchesPermanentPredicate(gd, target, predicate)).thenReturn(false);

        registry.getHandler(effect).resolve(gd, entry, effect);

        assertThat(target.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
    }
}
