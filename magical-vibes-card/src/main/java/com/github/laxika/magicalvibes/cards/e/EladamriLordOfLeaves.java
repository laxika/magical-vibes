package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "224")
public class EladamriLordOfLeaves extends Card {

    public EladamriLordOfLeaves() {
        // Other Elf creatures have forestwalk — every Elf creature on any battlefield;
        // ALL_CREATURES already excludes the source, which models the "other" wording.
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.ELF)));

        // Other Elves have shroud — any Elf permanent, not just creatures, so ALL_PERMANENTS
        // with an explicit "not the source" clause (that scope does not auto-exclude it).
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ALL_PERMANENTS,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.ELF),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())))));
    }
}
