package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DraftCardFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.DraftFromSpellbookEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "50")
public class HinterlandChef extends Card {

    private static final List<DraftFromSpellbookEffect.SpellbookCard> SPELLBOOK = List.of(
            new DraftFromSpellbookEffect.SpellbookCard("IKO", "143"),
            new DraftFromSpellbookEffect.SpellbookCard("HOU", "116"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "179"),
            new DraftFromSpellbookEffect.SpellbookCard("RTR", "117"),
            new DraftFromSpellbookEffect.SpellbookCard("KTK", "135"),
            new DraftFromSpellbookEffect.SpellbookCard("JUD", "121"),
            new DraftFromSpellbookEffect.SpellbookCard("ZEN", "168"),
            new DraftFromSpellbookEffect.SpellbookCard("ZNR", "190"),
            new DraftFromSpellbookEffect.SpellbookCard("ELD", "160"),
            new DraftFromSpellbookEffect.SpellbookCard("THB", "182"),
            new DraftFromSpellbookEffect.SpellbookCard("STX", "142"),
            new DraftFromSpellbookEffect.SpellbookCard("RNA", "145"),
            new DraftFromSpellbookEffect.SpellbookCard("MID", "181"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "222"),
            new DraftFromSpellbookEffect.SpellbookCard("VOW", "209"));

    public HinterlandChef() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DraftCardFromSpellbookEffect(SPELLBOOK, List.of(
                new PerpetuallyGrantCardCharacteristicsEffect(
                        CardType.ARTIFACT,
                        CardSubtype.FOOD,
                        new ActivatedAbility(
                                true,
                                "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this creature: You gain 3 life."
                        )))));
    }
}
