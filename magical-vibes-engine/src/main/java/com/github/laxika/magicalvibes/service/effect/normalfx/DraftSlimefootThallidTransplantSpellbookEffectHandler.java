package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.d.DeathbloomThallid;
import com.github.laxika.magicalvibes.cards.d.DeathbonnetSprout;
import com.github.laxika.magicalvibes.cards.f.FungalPlots;
import com.github.laxika.magicalvibes.cards.r.RhizomeLurcher;
import com.github.laxika.magicalvibes.cards.s.SaprolingMigration;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.cards.s.SporeSwarm;
import com.github.laxika.magicalvibes.cards.s.SporecrownThallid;
import com.github.laxika.magicalvibes.cards.s.Sporemound;
import com.github.laxika.magicalvibes.cards.s.SwarmShambler;
import com.github.laxika.magicalvibes.cards.t.ThallidOmnivore;
import com.github.laxika.magicalvibes.cards.t.ThallidSoothsayer;
import com.github.laxika.magicalvibes.cards.v.VerdantEmbrace;
import com.github.laxika.magicalvibes.cards.v.VerdantForce;
import com.github.laxika.magicalvibes.cards.y.YavimayaSapherd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DraftSlimefootThallidTransplantSpellbookEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Slimefoot, Thallid Transplant's fifteen-card spellbook draft. */
@Component
@RequiredArgsConstructor
public class DraftSlimefootThallidTransplantSpellbookEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DraftSlimefootThallidTransplantSpellbookEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() != null) {
            return;
        }

        List<Card> spellbook = createSpellbook(entry.getControllerId());
        Collections.shuffle(spellbook, ThreadLocalRandom.current());
        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData,
                new com.github.laxika.magicalvibes.model.PendingInteraction.SlimefootThallidTransplantSpellbookDraftChoice(
                        entry.getControllerId(), entry.getSourcePermanentId(),
                        List.copyOf(spellbook.subList(0, 3))));
    }

    private List<Card> createSpellbook(UUID ownerId) {
        List<Card> spellbook = new ArrayList<>(List.of(
                new DeathbloomThallid(),
                new DeathbonnetSprout(),
                new RhizomeLurcher(),
                new SporeCrawler(),
                new SporecrownThallid(),
                new Sporemound(),
                new SwarmShambler(),
                new ThallidOmnivore(),
                new ThallidSoothsayer(),
                new YavimayaSapherd(),
                new FungalPlots(),
                new VerdantForce(),
                new VerdantEmbrace(),
                new SporeSwarm(),
                new SaprolingMigration()));
        spellbook.forEach(card -> {
            card.setOwnerId(ownerId);
            card.setToken(false);
            card.setTokenCard(false);
            card.freeze();
        });
        return spellbook;
    }
}
