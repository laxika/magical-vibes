package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDFT", collectorNumber = "26")
public class SupportSkyforge extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("AER", "153"),
            new DraftFromSpellbookEffect.SpellbookCard("NEO", "247"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "233"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "235"),
            new DraftFromSpellbookEffect.SpellbookCard("AER", "142"),
            new DraftFromSpellbookEffect.SpellbookCard("DFT", "1"),
            new DraftFromSpellbookEffect.SpellbookCard("DFT", "47"),
            new DraftFromSpellbookEffect.SpellbookCard("KLD", "234"));

    public SupportSkyforge() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(4, "Servo", 1, 1, null,
                        List.of(CardSubtype.SERVO), Set.of(), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.ON_ATTACK, DraftCardFromSpellbookEffect.withArtifactCreatureGrant(SPELLBOOK));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(4), AnimatePermanentsEffect.crew()),
                "Crew 4"
        ));
    }
}
